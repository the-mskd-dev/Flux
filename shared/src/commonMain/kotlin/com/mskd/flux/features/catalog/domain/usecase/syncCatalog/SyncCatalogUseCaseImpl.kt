package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.data.datastore.SettingsDataStore
import com.mskd.flux.core.data.datastore.UserDataStore
import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.core.domain.model.artwork.Episode
import com.mskd.flux.core.domain.model.artwork.Media
import com.mskd.flux.core.domain.model.artwork.Movie
import com.mskd.flux.core.domain.model.artwork.Season
import com.mskd.flux.core.domain.model.artwork.Status
import com.mskd.flux.core.domain.model.catalog.Catalog
import com.mskd.flux.core.domain.model.catalog.CatalogFolder
import com.mskd.flux.core.domain.model.core.AppInfo
import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.features.tmdb.data.dto.EpisodeDto
import com.mskd.flux.features.tmdb.data.mapper.toDomain
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

internal class SyncCatalogUseCaseImpl(
    private val tmdb: TmdbDataSource,
    private val database: DatabaseRepository,
    private val user: UserDataStore,
    private val settings: SettingsDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val appInfo: AppInfo,
    private val coordinator: CatalogSyncCoordinator,
    private val getFileDurationUseCase: GetFileDurationUseCase,
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val filterExistingFilesUseCase: FilterExistingFilesUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(10)
) : SyncCatalogUseCase {

    private companion object { const val TAG = "SyncCatalogUseCase" }

    private data class ArtworkFolder(val artwork: Artwork, val files: List<UserFile>)

    override val state: StateFlow<SyncState> = coordinator.state

    override fun invoke(onlyNew: Boolean) {

        if (coordinator.isBusy && (coordinator.state.value as? SyncState.Syncing)?.full == true && onlyNew)
            return

        coordinator.launch(full = !onlyNew) {

            val dbMovies = database.getMovies()
            val dbEpisodes = database.getEpisodes()
            val dbFiles = filterExistingFilesUseCase(files = (dbMovies + dbEpisodes).map { it.file })
            val deviceFiles = getDeviceFilesUseCase()

            val newFiles = if (!onlyNew) deviceFiles else {
                deviceFiles.filter { file -> dbFiles.none { it.name == file.name } }
            }

            if (newFiles.isEmpty()) {
                database.deleteMediasNotInFiles(deviceFiles)
                user.setSyncTime(System.currentTimeMillis())
                return@launch
            }

            val folders = newFiles.groupInFolders()

            /*
                Count all steps
                1. Get Artworks
                2. Get all media for files (newFiles.size)
                3. Clean catalog
                4. Save artworks
                5. Save movies
                6. Save seasons
                7. Save episodes
             */
            coordinator.setTotalSteps(folders.size + newFiles.size + 5)

            var catalog = getCatalog(files = newFiles) { coordinator.incrementProgress() }
            catalog = applyCurrentMediaProgress(catalog, dbMovies, dbEpisodes)

            if (onlyNew) database.deleteMediasNotInFiles(deviceFiles) else database.deleteAll()
            coordinator.incrementProgress()

            database.saveArtworks(catalog.artworks); coordinator.incrementProgress()
            database.saveMovies(catalog.movies); coordinator.incrementProgress()
            database.saveSeasons(catalog.seasons); coordinator.incrementProgress()
            database.saveEpisodes(catalog.episodes); coordinator.incrementProgress()

            imagesPrefetchManager.prefetchImages()
            user.setSyncTime(System.currentTimeMillis())
            user.setVersionCode(appInfo.versionCode)

        }

    }

    private suspend fun getCatalog(
        files: List<UserFile>,
        updateProgress: () -> Unit
    ) : Catalog {

        val folders = files.groupInFolders()

        // Get data
        val artworksFolders = getArtworksFolders(
            folders = folders,
            updateProgress = updateProgress
        )

        val (movies, seasonsAndTmdbEpisodes) = supervisorScope {
            val moviesDeferred = async {
                runCatching { getMovies(artworkFolders = artworksFolders, updateProgress = updateProgress) }
                    .onFailure { Trace.error(TAG, "getMovies failed", it) }
                    .getOrElse { emptyList() }
            }
            val seasonsAndTmdbEpisodesDeferred = async {
                runCatching { getSeasonsAndTmdbEpisodes(artworkFolders = artworksFolders) }
                    .onFailure { Trace.error(TAG, "getSeasons failed", it) }
                    .getOrElse { emptyList() }
            }

            moviesDeferred.await() to seasonsAndTmdbEpisodesDeferred.await()
        }

        val seasons = seasonsAndTmdbEpisodes.map { it.first }
        val tmdbEpisodes = seasonsAndTmdbEpisodes.flatMap { it.second }

        val episodes = getEpisodes(
            artworkFolders = artworksFolders,
            episodesDto = tmdbEpisodes,
            updateProgress = updateProgress
        )

        return Catalog(
            artworks = artworksFolders.map { it.artwork },
            movies = movies.filterIsInstance<Movie>(),
            seasons = seasons,
            episodes = episodes + movies.filterIsInstance<Episode>()
        )

    }

    /**
     * Copies watch status and current time from existing database media to matched new items.
     */
    private fun applyCurrentMediaProgress(catalog: Catalog, dbMovies: List<Movie>, dbEpisodes: List<Episode>) : Catalog {

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

        Trace.info(TAG, "Apply progress on $count new media(s)")

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
    private suspend fun getArtworksFolders(
        folders: List<CatalogFolder>,
        updateProgress: () -> Unit
    ) : List<ArtworkFolder> {

        val artworkFolders = supervisorScope {

            folders.map { folder ->

                async(dispatcher) {

                    try {

                        val tmdbArtwork = tmdb.getTmdbArtwork(file = folder.files.first())

                        val artwork = tmdbArtwork?.toDomain() ?: Artwork.UNKNOWN

                        ArtworkFolder(
                            artwork = artwork,
                            files = folder.files
                        )

                    } catch (e: Exception) {
                        Trace.error(TAG, "getArtworksFolders - Fail to get ArtworkFolder for ${folder.files.first().name}", e)
                        ArtworkFolder(
                            artwork = Artwork.UNKNOWN,
                            files = folder.files
                        )
                    } finally {
                        updateProgress()
                    }

                }

            }.awaitAll()

        }

        Trace.info(TAG, "Found ${artworkFolders.size} artwork(s)")

        return artworkFolders

    }

    /**
     * Filters movie folders and fetches movie metadata details from TMDB in parallel.
     */
    private suspend fun getMovies(
        artworkFolders: List<ArtworkFolder>,
        updateProgress: () -> Unit
    ) : List<Media> {

        val movies = supervisorScope {

            artworkFolders.filter { it.artwork.type == ContentType.MOVIE }.map { (artwork, files) ->

                async(dispatcher) {

                    try {

                        val file = files.first()

                        when {
                            artwork.id == Artwork.UNKNOWN_ID -> Episode(file = file)
                            else -> {

                                val tmdbMovie = tmdb.getTmdbMovie(artworkId = artwork.id)

                                tmdbMovie?.toDomain(
                                    file = file,
                                    duration = tmdbMovie.duration ?: getFileDurationUseCase(file = file)
                                ) ?: createUnknownMedia(file = file)

                            }
                        }

                    } catch (e: Exception) {
                        Trace.error(TAG, "[getMovies] Fail to get movie from ${files.first().name}", e)
                        null
                    } finally {
                        updateProgress()
                    }

                }

            }.awaitAll().filterNotNull()

        }

        Trace.info(TAG, "Found ${movies.size} movie(s)")

        return movies

    }

    private suspend fun getSeasonsAndTmdbEpisodes(artworkFolders: List<ArtworkFolder>) : List<Pair<Season, List<EpisodeDto>>> {

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
                                    it.toDomain(artworkId = artwork.id) to it.episodes
                                }

                            } catch (e: Exception) {
                                Trace.error(
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

    private suspend fun getEpisodes(
        artworkFolders: List<ArtworkFolder>,
        episodesDto: List<EpisodeDto>,
        updateProgress: () -> Unit
    ) : List<Episode> {

        val language = settings.getDataLanguage()

        val tmdbEpisodesMap = episodesDto.associateBy { Triple(it.artworkId, it.season, it.number) }

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

                                    var tmdbEpisode = tmdbEpisodesMap[Triple(artwork.id, season, number)]

                                    if (tmdbEpisode == null) {
                                        createUnknownMedia(file = file)
                                    } else {

                                        if (tmdbEpisode.title.isBlank() || tmdbEpisode.description.isBlank()) {

                                            tmdbEpisode = tmdb.translateTmdbEpisode(
                                                artworkId = artwork.id,
                                                episodeDto = tmdbEpisode,
                                                language = language
                                            )

                                        }

                                        tmdbEpisode.toDomain(
                                            artworkId = artwork.id,
                                            file = file,
                                            duration = tmdbEpisode.duration
                                                ?: getFileDurationUseCase(file = file)
                                        )
                                    }

                                }
                                else -> null
                            }

                        } catch (e: Exception) {
                            Trace.error(TAG, "[getEpisodes] Fail to get episode from ${file.name}", e)
                            null
                        } finally {
                            updateProgress()
                        }

                    }

                }.awaitAll().filterNotNull()

            }

        }

        Trace.info(TAG, "Found ${episodes.size} episode(s)")

        return episodes

    }

    /**
     * Creates a fallback [Episode] with unknown metadata and extracts duration using MediaMetadataRetriever.
     */
    private suspend fun createUnknownMedia(file: UserFile) : Episode = withContext(dispatcher) {

        Trace.info(TAG, "Create unknown media for ${file.name}")

        val duration = getFileDurationUseCase(file = file)
        Episode(file = file, duration = duration)

    }

}