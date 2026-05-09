package com.mskd.flux.useCases.catalog

import android.util.Log
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.source.media.MediaSourceTMDBImpl.Companion.TAG
import com.mskd.flux.model.Catalog
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.UserFolder
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface CatalogUC {

    fun flowArtworks() : Flow<List<Artwork>>
    fun syncCatalog(onlyNew: Boolean)
    suspend fun cleanCatalog()

}

class CatalogUCImpl(
    private val tmdb: TmdbRepository,
    private val database: DatabaseRepository,
    private val files: FilesRepository,
    private val user: UserRepository,
    private val scope: CoroutineScope
) : CatalogUC {

    //region Data class
    private data class ArtworkFolder(
        val artwork: Artwork,
        val files: List<UserFile>
    )

    //endregion

    //region Coroutines

    private var syncJob: Job? = null

    //endregion

    //region Public methods

    override fun flowArtworks(): Flow<List<Artwork>> {
        return database.flowArtworks()
    }

    override fun syncCatalog(onlyNew: Boolean) {

        syncJob?.cancel()

        syncJob = scope.launch {

            // Get files
            val allFiles = files.getFiles()
            val newFiles = if (!onlyNew) { allFiles } else {
                val dbFilesNames = database.getAllFileNames()
                allFiles.filter { !dbFilesNames.contains(it.name) }
            }

            if (newFiles.isEmpty()) {
                user.setSyncTime(System.currentTimeMillis())
                return@launch
            }

            // Get data
            var catalog = getCatalog(files = newFiles)

            if (!onlyNew) {
                catalog = applyCurrentProgress(catalog = catalog)
            }

            // Save data
            database.saveArtworks(artworks = catalog.artworks)
            database.saveMovies(movies = catalog.movies)
            database.saveEpisodes(episodes = catalog.episodes)

            // Save time
            user.setSyncTime(System.currentTimeMillis())

        }

    }

    override suspend fun cleanCatalog() {

        val allFiles = files.getFiles()
        database.deleteMediasNotInFiles(allFiles)

        tryToRetrieveUnknownMedias()

    }

    //endregion

    //region Private methods

    private suspend fun getCatalog(files: List<UserFile>) : Catalog {

        val folders = files.groupInFolders()

        // Get data
        val artworksFolders = getArtworksFolders(folders = folders)
        val movies = getMovies(artworkFolders = artworksFolders)
        val episodes = getEpisodes(artworkFolders = artworksFolders)

        return Catalog(
            artworks = artworksFolders.map { it.artwork },
            movies = movies.filterIsInstance<Movie>(),
            episodes = movies.filterIsInstance<Episode>() + episodes
        )

    }

    private suspend fun applyCurrentProgress(catalog: Catalog) : Catalog {

        val dbMovies = database.getMovies()
        val dbEpisodes = database.getEpisodes()

        val movies = catalog.movies.map { newMovie ->

            dbMovies.find { it.mediaId == newMovie.mediaId }?.let { oldMovie ->

                newMovie.copy(
                    currentTime = oldMovie.currentTime,
                    status = oldMovie.status
                )

            } ?: newMovie

        }

        val episodes = catalog.episodes.map { newEpisode ->

            dbEpisodes.find { it.mediaId == newEpisode.mediaId }?.let { oldEpisode ->

                newEpisode.copy(
                    currentTime = oldEpisode.currentTime,
                    status = oldEpisode.status
                )

            } ?: newEpisode

        }

        return Catalog(
            artworks = catalog.artworks,
            movies = movies,
            episodes = episodes
        )

    }

    private suspend fun getArtworksFolders(folders: List<UserFolder>) : List<ArtworkFolder> {

        return coroutineScope {

            folders.map { folder ->

                async {

                    val tmdbArtwork = tmdb.getTmdbArtwork(file = folder.files.first())

                    val artwork = if (tmdbArtwork == null)
                        Artwork.UNKNOWN
                    else
                        Artwork(tmdbArtwork = tmdbArtwork)

                    ArtworkFolder(
                        artwork = artwork,
                        files = folder.files
                    )

                }

            }.awaitAll()

        }

    }

    private suspend fun getMovies(artworkFolders: List<ArtworkFolder>) : List<Media> {

        return coroutineScope {

            artworkFolders.filter { it.artwork.type == ContentType.MOVIE }.map { (artwork, files) ->

                async {

                    val tmdbMovie = tmdb.getTmdbMovie(artworkId = artwork.id)

                    if (tmdbMovie == null)
                        Episode(file = files.first())
                    else
                        Movie(tmdbMovie = tmdbMovie, file = files.first())

                }

            }.awaitAll()

        }

    }

    private suspend fun getEpisodes(artworkFolders: List<ArtworkFolder>) : List<Episode> {

        return coroutineScope {

            artworkFolders.filter { it.artwork.type == ContentType.SHOW }.flatMap { (artwork, files) ->

                files.map { file ->

                    val season = file.nameProperties.season
                    val number = file.nameProperties.episode

                    async {

                        if (season != null && number != null) {

                            val tmdbEpisode = tmdb.getTmdbEpisode(
                                artworkId = artwork.id,
                                season = season,
                                number = number
                            )

                            if (tmdbEpisode == null)
                                Episode(file = file)
                            else
                                Episode(tmdbEpisode = tmdbEpisode, file = file,)

                        } else {
                            null
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }

    }

    private suspend fun tryToRetrieveUnknownMedias() {

        try {

            val unknownMedias = database.getUnknownMedias()
            val files = unknownMedias.map { it.file }

            val (newArtworks, newMovies, newEpisodes) = getCatalog(files = files)

            val moviesToSave = arrayListOf<Movie>()
            val episodesToSave = arrayListOf<Episode>()
            val mediasToDelete = arrayListOf<Episode>()

            newMovies.forEach { movie ->

                unknownMedias.find { it.file == movie.file }?.let { unknownMedia ->

                    val newMovie = movie.copy(
                        currentTime = unknownMedia.currentTime,
                        status = unknownMedia.status
                    )

                    moviesToSave.add(newMovie)
                    mediasToDelete.add(unknownMedia)

                }

            }

            newEpisodes.filter { !it.isUnknown }.forEach { episode ->

                unknownMedias.find { it.file == episode.file }?.let { unknownMedia ->

                    val newEpisode = episode.copy(
                        currentTime = unknownMedia.currentTime,
                        status = unknownMedia.status
                    )

                    episodesToSave.add(newEpisode)
                    mediasToDelete.add(unknownMedia)

                }

            }

            database.saveArtworks(newArtworks.filter { !it.isUnknown })
            database.saveMovies(moviesToSave)
            database.saveEpisodes(episodesToSave)
            database.deleteEpisodes(mediasToDelete)

        } catch (e: Exception) {
            Log.e(TAG, "Fail to retrieve unknown medias", e)
        }

    }

    //endregion

}