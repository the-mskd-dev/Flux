package com.mskd.flux.useCases.catalog

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.net.toUri
import com.mskd.flux.BuildConfig
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
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
import com.mskd.flux.model.tmdb.TMDBTranslations
import com.mskd.flux.model.tmdb.findWithLocale
import com.mskd.flux.useCases.images.ImagesUC
import com.mskd.flux.utils.extensions.groupInFolders
import com.mskd.flux.utils.extensions.msToMin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

interface CatalogUC {

    val state: Flow<State>
    val artworks : Flow<List<Artwork>>
    fun syncCatalog(onlyNew: Boolean)

    suspend fun getCatalog(files: List<UserFile>) : Catalog

    suspend fun cleanCatalog()

    fun updateLanguage()

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
    private val settings: SettingsRepository,
    private val imagesUC: ImagesUC,
    private val scope: CoroutineScope,
    private val context: Context
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
    private var translationJob: Job? = null

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
        translationJob?.cancel()

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

            Log.d(TAG, "Found ${newFiles.size} new file(s)")
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

            // Clean catalog
            if (onlyNew) {
                database.deleteMediasNotInFiles(deviceFiles)
            } else {
                database.deleteAll()
            }

            // Save data
            database.saveArtworks(artworks = catalog.artworks)
            database.saveMovies(movies = catalog.movies)
            database.saveEpisodes(episodes = catalog.episodes)

            // Pre-fetch images if needed
            if (settings.flow.first().prefetchImages)
                imagesUC.prefetchImages()

            // Save time and version code
            user.setSyncTime(System.currentTimeMillis())
            user.setVersionCode(BuildConfig.VERSION_CODE)
            _state.value = CatalogUC.State.Idle

        }

    }

    override suspend fun getCatalog(files: List<UserFile>) : Catalog {

        val folders = files.groupInFolders()

        // Get data
        val artworksFolders = getArtworksFolders(folders = folders)

        val (movies, episodes) = supervisorScope {
            val moviesDeferred = async {
                runCatching { getMovies(artworkFolders = artworksFolders) }
                    .onFailure { Log.e(TAG, "getMovies failed", it) }
                    .getOrElse { emptyList() }
            }
            val episodesDeferred = async {
                runCatching { getEpisodes(artworkFolders = artworksFolders) }
                    .onFailure { Log.e(TAG, "getEpisodes failed", it) }
                    .getOrElse { emptyList() }
            }
            moviesDeferred.await() to episodesDeferred.await()
        }

        return Catalog(
            artworks = artworksFolders.map { it.artwork },
            movies = movies.filterIsInstance<Movie>(),
            episodes = movies.filterIsInstance<Episode>() + episodes
        )

    }

    override suspend fun cleanCatalog() {

        val allFiles = files.getFiles()
        database.deleteMediasNotInFiles(allFiles)

    }

    override fun updateLanguage() {

        translationJob?.cancel()

        translationJob = scope.launch {

            _state.value = CatalogUC.State.Syncing(full = false)

            val language = settings.getDataLanguage()
            val movies = database.getMovies()
            val episodes = database.getEpisodes()

            var translatedMovies: List<Movie> = emptyList()
            var translatedEpisodes: List<Episode> = emptyList()

            supervisorScope {

                translatedMovies = movies.map { movie ->

                    async(dispatcher) {

                        tmdb.getTmdbTranslations(
                            request = TMDBTranslations.Request.Movie(artworkId = movie.artworkId),
                            language = language
                        )?.let { translation ->

                            movie.copy(
                                title = translation.data.name ?: movie.title,
                                description = translation.data.overview ?: movie.description
                            )

                        }

                    }

                }.awaitAll().filterNotNull()

                translatedEpisodes = episodes.map { episode ->

                    async(dispatcher) {

                        tmdb.getTmdbTranslations(
                            request = TMDBTranslations.Request.Episode(
                                artworkId = episode.artworkId,
                                season = episode.season,
                                number = episode.number
                            ),
                            language = language
                        )?.let { translation ->

                            episode.copy(
                                title = translation.data.name ?: episode.title,
                                description = translation.data.overview ?: episode.description
                            )

                        }

                    }

                }.awaitAll().filterNotNull()


            }

            database.saveMovies(translatedMovies)
            database.saveEpisodes(translatedEpisodes)

            _state.value = CatalogUC.State.Idle

        }

    }

    //endregion

    //region Private methods

    private fun applyCurrentProgress(catalog: Catalog, dbMovies: List<Movie>, dbEpisodes: List<Episode>) : Catalog {

        var count = 0

        val movies = catalog.movies.map { newMovie ->

            dbMovies.find { it.file.name == newMovie.file.name && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldMovie ->

                count++

                newMovie.copy(
                    currentTime = oldMovie.currentTime,
                    status = oldMovie.status
                )

            } ?: newMovie

        }

        val episodes = catalog.episodes.map { newEpisode ->

            dbEpisodes.find { it.file.name == newEpisode.file.name && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldEpisode ->

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

        val artworkFolders = supervisorScope {

            folders.map { folder ->

                async(dispatcher) {

                    try {

                        val tmdbArtwork = tmdb.getTmdbArtwork(file = folder.files.first())

                        val artwork = if (tmdbArtwork == null)
                            Artwork.UNKNOWN
                        else
                            Artwork(tmdbArtwork = tmdbArtwork)

                        ArtworkFolder(
                            artwork = artwork,
                            files = folder.files
                        )

                    } catch (e: Exception) {
                        Log.e(TAG, "getArtworksFolders - Fail to get ArtworkFolder for ${folder.files.first().name}", e)
                        ArtworkFolder(
                            artwork = Artwork.UNKNOWN,
                            files = folder.files
                        )
                    }

                }

            }.awaitAll()

        }

        Log.i(TAG, "Found ${artworkFolders.size} artwork(s)")

        return artworkFolders

    }

    private suspend fun getMovies(artworkFolders: List<ArtworkFolder>) : List<Media> {

        val movies = supervisorScope {

            artworkFolders.filter { it.artwork.type == ContentType.MOVIE }.map { (artwork, files) ->

                async(dispatcher) {

                    try {

                        when {
                            artwork.id == Artwork.UNKNOWN_ID -> Episode(file = files.first())
                            else -> {

                                val tmdbMovie = tmdb.getTmdbMovie(artworkId = artwork.id)

                                if (tmdbMovie == null)
                                    createUnknownMedia(file = files.first())
                                else
                                    Movie(tmdbMovie = tmdbMovie, file = files.first())

                            }
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "[getMovies] Fail to get movie from ${files.first().name}", e)
                        null
                    }

                }

            }.awaitAll().filterNotNull()

        }

        Log.i(TAG, "Found ${movies.size} movie(s)")

        return movies

    }

    private suspend fun getEpisodes(artworkFolders: List<ArtworkFolder>) : List<Episode> {

        val episodes = supervisorScope {

            artworkFolders.filter { it.artwork.type == ContentType.SHOW }.flatMap { (artwork, files) ->

                files.map { file ->

                    async(dispatcher) {

                        try {

                            val season = file.nameProperties.season
                            val number = file.nameProperties.episode

                            when {
                                artwork.id == Artwork.UNKNOWN_ID -> createUnknownMedia(file = file)
                                season != null && number != null -> {

                                    val tmdbEpisode = tmdb.getTmdbEpisode(
                                        artworkId = artwork.id,
                                        season = season,
                                        number = number
                                    )

                                    if (tmdbEpisode == null)
                                        createUnknownMedia(file = file)
                                    else
                                        Episode(tmdbEpisode = tmdbEpisode, file = file,)

                                }
                                else -> null
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "[getEpisodes] Fail to get episode from ${file.name}", e)
                            null
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }

        Log.i(TAG, "Found ${episodes.size} episode(s)")

        return episodes

    }

    private suspend fun createUnknownMedia(file: UserFile) : Episode = withContext(dispatcher) {

        Log.i(TAG, "Create unknown media for ${file.name}")

        val retriever = MediaMetadataRetriever()

        try {

            val duration = context.contentResolver.openAssetFileDescriptor(file.path.toUri(), "r")?.use { afd ->
                retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                val durationInMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                durationInMs.msToMin.toInt()
            } ?: 0

            Episode(file = file, duration = duration)

        } catch (e: Exception) {

            Log.e(TAG, "[createUnknownMedia] Fail to get duration for ${file.path}", e)
            Episode(file = file)

        } finally {

            retriever.release()

        }

    }

    //endregion

}