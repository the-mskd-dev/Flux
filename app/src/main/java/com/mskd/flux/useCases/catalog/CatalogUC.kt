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
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.model.artwork.Show
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.model.tmdb.TMDBEpisode
import com.mskd.flux.model.tmdb.TMDBTranslations
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
import java.util.Locale

/**
 * Use case interface for managing the media catalog.
 */
interface CatalogUC {

    /**
     * Flow representing the current state of catalog synchronization.
     */
    val state: Flow<State>

    /**
     * Flow emitting the list of all artworks saved in the database.
     */
    val artworks : Flow<List<Artwork>>

    /**
     * Synchronizes the local media catalog with files on the device.
     *
     * It scans the media files, queries TMDB for metadata, and updates the database.
     * If [onlyNew] is true, it only processes newly added files.
     */
    fun syncCatalog(onlyNew: Boolean)

    /**
     * Retrieves and constructs a [Catalog] object from a list of user files.
     *
     * It groups files into folders and parallelly fetches artwork metadata.
     */
    suspend fun getCatalog(files: List<UserFile>) : Catalog

    /**
     * Cleans up the database catalog by removing entries of files that no longer exist.
     */
    suspend fun cleanCatalog()

    /**
     * Re-fetches localized details (translations) for all items currently in the catalog.
     */
    fun updateLanguage()

    /**
     * Represents the current state of the catalog synchronization.
     */
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

    /**
     * Cancels active operations and launches a coroutine to sync files with the database.
     *
     * It scans the media folder, checks TMDB for metadata, preserves playback progress, and saves the new catalog.
     */
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
            database.saveSeasons(seasons = catalog.seasons)
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

    /**
     * Groups user files into directory folders and asynchronously retrieves movies and shows details.
     */
    override suspend fun getCatalog(files: List<UserFile>) : Catalog {

        val folders = files.groupInFolders()

        // Get data
        val artworksFolders = getArtworksFolders(folders = folders)

        val (movies, seasons, episodes) = supervisorScope {
            val moviesDeferred = async {
                runCatching { getMovies(artworkFolders = artworksFolders) }
                    .onFailure { Log.e(TAG, "getMovies failed", it) }
                    .getOrElse { emptyList() }
            }
            val seasonsDeferred = async {
                runCatching { getSeasons(artworkFolders = artworksFolders) }
                    .onFailure { Log.e(TAG, "getSeasons failed", it) }
                    .getOrElse { emptyList() }
            }
            val episodesDeferred = async {
                runCatching { getEpisodes(artworkFolders = artworksFolders) }
                    .onFailure { Log.e(TAG, "getEpisodes failed", it) }
                    .getOrElse { emptyList() }
            }

            Triple(
                moviesDeferred.await(),
                seasonsDeferred.await(),
                episodesDeferred.await()
            )
        }

        return Catalog(
            artworks = artworksFolders.map { it.artwork },
            movies = movies.filterIsInstance<Movie>(),
            seasons = seasons,
            episodes = episodes + movies.filterIsInstance<Episode>()
        )

    }

    /**
     * Discovers all current files on disk and removes missing media from the database catalog.
     */
    override suspend fun cleanCatalog() {

        val allFiles = files.getFiles()
        database.deleteMediasNotInFiles(allFiles)

    }

    /**
     * Asynchronously updates the language metadata of stored movies, seasons, and episodes via TMDB.
     */
    override fun updateLanguage() {

        translationJob?.cancel()

        translationJob = scope.launch {

            _state.value = CatalogUC.State.Syncing(full = false)

            val language = settings.getDataLanguage()
            val movies = database.getMovies()
            val seasons = database.getSeasons()
            val episodes = database.getEpisodes()

            var translatedMovies: List<Movie> = emptyList()
            var translatedSeasons: List<Season> = emptyList()
            var translatedEpisodes: List<Episode> = emptyList()

            supervisorScope {

                translatedMovies = movies.map { movie ->

                    async(dispatcher) {

                        tmdb.getTmdbTranslation(
                            request = TMDBTranslations.Request.Movie(
                                artworkId = movie.artworkId,
                                language = language
                            ),
                        )?.let { translation ->

                            movie.copy(
                                title = translation.data.name ?: movie.title,
                                description = translation.data.overview ?: movie.description
                            )

                        }

                    }

                }.awaitAll().filterNotNull()

                translatedSeasons = seasons.map { season ->

                    async(dispatcher) {

                        tmdb.getTmdbTranslation(
                            request = TMDBTranslations.Request.Season(
                                artworkId = season.artworkId,
                                season = season.season,
                                language = language
                            ),
                        )?.let { translation ->

                            season.copy(
                                title = translation.data.name ?: season.title,
                                description = translation.data.overview ?: season.description
                            )

                        }

                    }

                }.awaitAll().filterNotNull()

                translatedEpisodes = episodes.map { episode ->

                    async(dispatcher) {

                        tmdb.getTmdbTranslation(
                            request = TMDBTranslations.Request.Episode(
                                artworkId = episode.artworkId,
                                season = episode.season,
                                number = episode.number,
                                language = language
                            ),
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
            database.saveSeasons(translatedSeasons)
            database.saveEpisodes(translatedEpisodes)

            _state.value = CatalogUC.State.Idle

        }

    }

    //endregion

    //region Private methods

    /**
     * Copies watch status and current time from existing database media to matched new items.
     */
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
            seasons = catalog.seasons,
            episodes = episodes
        )

    }

    /**
     * Queries TMDB to associate each user folder with an [Artwork] based on its files.
     */
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

    /**
     * Filters movie folders and fetches movie metadata details from TMDB in parallel.
     */
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

    private suspend fun getSeasons(artworkFolders: List<ArtworkFolder>) : List<Season> {

        val folders = artworkFolders.filter { it.artwork.type == ContentType.SHOW && it.artwork.id != Artwork.UNKNOWN_ID }

        val seasons = supervisorScope {

            folders.flatMap { (artwork, files) ->

                files
                    .map { it.season }
                    .distinct()
                    .filterNotNull()
                    .map { season ->

                        async(dispatcher) {

                            try {

                                tmdb.getTmdbSeason(artworkId = artwork.id, season = season)?.let {
                                    Season(tmdbSeason = it, artworkId = artwork.id)
                                }

                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "getSeasons - Fail to get season for artworkId ${artwork.id} - season $season",
                                    e
                                )
                                null
                            }

                        }

                    }.awaitAll().filterNotNull()

            }

        }

        return seasons

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

                                    if (tmdbEpisode == null) {
                                        createUnknownMedia(file = file)
                                    } else {
                                        Episode(
                                            tmdbEpisode = tmdbEpisode,
                                            artworkId = artwork.id,
                                            file = file
                                        )
                                    }

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

    /**
     * Creates a fallback [Episode] with unknown metadata and extracts duration using MediaMetadataRetriever.
     */
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