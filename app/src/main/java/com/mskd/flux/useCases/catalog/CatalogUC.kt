package com.mskd.flux.useCases.catalog

import android.util.Log
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.model.Catalog
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.UserFolder
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface CatalogUC {

    val state: Flow<State>
    val artworks : Flow<List<Artwork>>
    fun syncCatalog(onlyNew: Boolean)
    suspend fun cleanCatalog()

    sealed class State {
        data object Idle: State()
        data class Syncing(val full: Boolean) : State()
    }

}

class CatalogUCImpl(
    private val tmdb: TmdbRepository,
    private val database: DatabaseRepository,
    private val files: FilesRepository,
    private val user: UserRepository,
    private val scope: CoroutineScope
) : CatalogUC {

    private companion object {
        const val TAG = "CatalogUCImpl"
    }

    //region Data class
    private data class ArtworkFolder(
        val artwork: Artwork,
        val files: List<UserFile>
    )

    //endregion

    //region Variables

    private var syncJob: Job? = null

    private var _state = MutableStateFlow<CatalogUC.State>(CatalogUC.State.Idle)

    private val dispatcher = Dispatchers.IO.limitedParallelism(10)

    //endregion

    //region Public methods

    override val state: Flow<CatalogUC.State> = _state.asStateFlow()

    override val artworks: Flow<List<Artwork>> = database.flowArtworks()

    override fun syncCatalog(onlyNew: Boolean) {

        if ((_state.value as? CatalogUC.State.Syncing)?.full == true && onlyNew)
            return

        syncJob?.cancel()

        syncJob = scope.launch {

            _state.value = CatalogUC.State.Syncing(full = !onlyNew)

            // Get current medias
            val dbMovies = database.getMovies()
            val dbEpisodes = database.getEpisodes()
            val dbFiles = files.filterExistingFiles(files = (dbMovies + dbEpisodes).map { it.file })

            // Get files
            val deviceFiles = files.getFiles()
            val newFiles = if (!onlyNew) { deviceFiles } else {
                deviceFiles.filter { file -> dbFiles.none { it.name == file.name } }
            }

            Log.d(TAG, "Found $newFiles new file(s)")
            newFiles.forEach {
                Log.d(TAG, it.name)
            }

            if (newFiles.isEmpty()) {
                user.setSyncTime(System.currentTimeMillis())
                _state.value = CatalogUC.State.Idle
                return@launch
            }

            // Get data
            var catalog = getCatalog(files = newFiles)
            catalog = applyCurrentProgress(
                catalog = catalog,
                dbMovies = dbMovies,
                dbEpisodes = dbEpisodes
            )

            // Save data
            database.saveArtworks(artworks = catalog.artworks)
            database.saveMovies(movies = catalog.movies)
            database.saveEpisodes(episodes = catalog.episodes)

            // Clean catalog
            database.deleteMediasNotInFiles(deviceFiles)

            // Save time
            user.setSyncTime(System.currentTimeMillis())
            _state.value = CatalogUC.State.Idle

        }

    }

    override suspend fun cleanCatalog() {

        val allFiles = files.getFiles()
        database.deleteMediasNotInFiles(allFiles)

    }

    //endregion

    //region Private methods

    private suspend fun getCatalog(files: List<UserFile>) : Catalog {

        val folders = files.groupInFolders()

        // Get data
        val artworksFolders = getArtworksFolders(folders = folders)
        var movies: List<Media> = emptyList()
        var episodes: List<Episode> = emptyList()

        coroutineScope {
            launch {  movies = getMovies(artworkFolders = artworksFolders) }
            launch { episodes = getEpisodes(artworkFolders = artworksFolders) }
        }

        return Catalog(
            artworks = artworksFolders.map { it.artwork },
            movies = movies.filterIsInstance<Movie>(),
            episodes = movies.filterIsInstance<Episode>() + episodes
        )

    }

    private fun applyCurrentProgress(catalog: Catalog, dbMovies: List<Movie>, dbEpisodes: List<Episode>) : Catalog {

        var count = 0

        val movies = catalog.movies.map { newMovie ->

            dbMovies.find { it.mediaId == newMovie.mediaId && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldMovie ->

                count++

                newMovie.copy(
                    currentTime = oldMovie.currentTime,
                    status = oldMovie.status
                )

            } ?: newMovie

        }

        val episodes = catalog.episodes.map { newEpisode ->

            dbEpisodes.find { it.mediaId == newEpisode.mediaId && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldEpisode ->

                count++

                newEpisode.copy(
                    currentTime = oldEpisode.currentTime,
                    status = oldEpisode.status
                )

            } ?: newEpisode

        }

        Log.i(TAG, "Apply progress on $count new media(s)")

        return Catalog(
            artworks = catalog.artworks,
            movies = movies,
            episodes = episodes
        )

    }

    private suspend fun getArtworksFolders(folders: List<UserFolder>) : List<ArtworkFolder> {

        val artworkFolders = coroutineScope {

            folders.map { folder ->

                async(dispatcher) {

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

        Log.i(TAG, "Found ${artworkFolders.size} artwork(s)")

        return artworkFolders

    }

    private suspend fun getMovies(artworkFolders: List<ArtworkFolder>) : List<Media> {

        val movies = coroutineScope {

            artworkFolders.filter { it.artwork.type == ContentType.MOVIE }.map { (artwork, files) ->

                async(dispatcher) {

                    when {
                        artwork.id == Artwork.UNKNOWN_ID -> Episode(file = files.first())
                        else -> {

                            val tmdbMovie = tmdb.getTmdbMovie(artworkId = artwork.id)

                            if (tmdbMovie == null)
                                Episode(file = files.first())
                            else
                                Movie(tmdbMovie = tmdbMovie, file = files.first())

                        }
                    }

                }

            }.awaitAll()

        }

        Log.i(TAG, "Found ${movies.size} movie(s)")

        return movies

    }

    private suspend fun getEpisodes(artworkFolders: List<ArtworkFolder>) : List<Episode> {

        val episodes = coroutineScope {

            artworkFolders.filter { it.artwork.type == ContentType.SHOW }.flatMap { (artwork, files) ->

                files.map { file ->

                    val season = file.nameProperties.season
                    val number = file.nameProperties.episode

                    async(dispatcher) {

                        when {
                            artwork.id == Artwork.UNKNOWN_ID -> Episode(file = file)
                            season != null && number != null -> {

                                val tmdbEpisode = tmdb.getTmdbEpisode(
                                    artworkId = artwork.id,
                                    season = season,
                                    number = number
                                )

                                if (tmdbEpisode == null)
                                    Episode(file = file)
                                else
                                    Episode(tmdbEpisode = tmdbEpisode, file = file,)

                            }
                            else -> null
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }

        Log.i(TAG, "Found ${episodes.size} episode(s)")

        return episodes

    }

    //endregion

}