package com.mskd.flux.useCases.catalogUC

import android.util.Log
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

interface CatalogUC {

    fun flowArtworks() : Flow<List<Artwork>>
    suspend fun syncCatalog(onlyNew: Boolean)
    suspend fun cleanCatalog()

}

class CatalogUCImpl(
    private val tmdbRepository: TmdbRepository,
    private val databaseRepository: DatabaseRepository,
    private val filesRepository: FilesRepository
) : CatalogUC {

    //region Data class
    private data class ArtworkFolder(
        val artwork: Artwork,
        val files: List<UserFile>
    )

    //endregion

    //region Public methods

    override fun flowArtworks(): Flow<List<Artwork>> {
        return databaseRepository.flowArtworks()
    }

    override suspend fun syncCatalog(onlyNew: Boolean) {

        // Get files
        val allFiles = filesRepository.getFiles()
        val newFiles = if (!onlyNew) { allFiles } else {
            val dbFilesNames = databaseRepository.getAllFileNames()
            allFiles.filter { !dbFilesNames.contains(it.name) }
        }

        if (newFiles.isEmpty())
            return

        // Get data
        val catalog = getCatalog(files = newFiles)

        // Save data
        databaseRepository.saveArtworks(artworks = catalog.artworks)
        databaseRepository.saveMovies(movies = catalog.movies)
        databaseRepository.saveEpisodes(episodes = catalog.episodes)

    }

    override suspend fun cleanCatalog() {

        val allFiles = filesRepository.getFiles()
        databaseRepository.deleteMediasNotInFiles(allFiles)

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

    private suspend fun getArtworksFolders(folders: List<UserFolder>) : List<ArtworkFolder> {

        return coroutineScope {

            folders.map { folder ->

                async {

                    val tmdbArtwork = tmdbRepository.getTmdbArtwork(file = folder.files.first())

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

                    val tmdbMovie = tmdbRepository.getTmdbMovie(artworkId = artwork.id)

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

                            val tmdbEpisode = tmdbRepository.getTmdbEpisode(
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

            val unknownMedias = databaseRepository.getUnknownMedias()
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

            databaseRepository.saveArtworks(newArtworks.filter { !it.isUnknown })
            databaseRepository.saveMovies(moviesToSave)
            databaseRepository.saveEpisodes(episodesToSave)
            databaseRepository.deleteEpisodes(mediasToDelete)

        } catch (e: Exception) {
            Log.e(TAG, "Fail to retrieve unknown medias", e)
        }

    }

    //endregion

}