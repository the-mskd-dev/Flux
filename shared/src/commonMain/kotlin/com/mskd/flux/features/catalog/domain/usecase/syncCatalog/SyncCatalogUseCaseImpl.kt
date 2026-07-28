package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.catalog.Catalog
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkFolderFetcher
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcher
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.resolver.EpisodeResolver
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope

class SyncCatalogUseCaseImpl(
    private val database: DatabaseRepository,
    private val user: UserDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val appInfo: AppInfo,
    private val coordinator: CatalogSyncCoordinator,
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val filterExistingFilesUseCase: FilterExistingFilesUseCase,
    private val artworkFolderFetcher: ArtworkFolderFetcher,
    private val movieMetadataFetcher: MovieMetadataFetcher,
    private val seasonMetadataFetcher: SeasonMetadataFetcher,
    private val episodeResolver: EpisodeResolver,
) : SyncCatalogUseCase {

    private companion object { const val TAG = "SyncCatalogUseCase" }

    override val state: StateFlow<SyncState> = coordinator.state

    override fun invoke(onlyNew: Boolean) {

        if (coordinator.isBusy && (coordinator.state.value as? SyncState.Syncing)?.full == true && onlyNew)
            return

        coordinator.launch(full = !onlyNew) {

            val dbMedias = database.getMedias()
            val existingFiles = filterExistingFilesUseCase(files = (dbMedias).map { it.file })
            val deviceFiles = getDeviceFilesUseCase()

            // TODO: Delete in October 2026
            // Get old unknown files
            val unknownFiles = dbMedias.filter { it is Episode && it.isUnknown }
                .map { it.file }
                .filter { file -> existingFiles.any { it.path == file.path } && file.realPath.isEmpty() }

            val newFiles = if (!onlyNew) deviceFiles else {
                deviceFiles.filter { file -> existingFiles.none { it.path == file.path } } + unknownFiles
            }

            if (newFiles.isEmpty()) {
                database.deleteMediasNotInFiles(existingFiles)

                // TODO: Delete in October 2026
                database.updateRealPaths(files = deviceFiles)

                user.setSyncTime(System.currentTimeMillis())
                user.setVersionCode(appInfo.versionCode)
                return@launch
            }

            val folders = newFiles.groupInFolders()

            /*
                Count all steps
                1. Get Artworks
                2. Get all media for files (newFiles.size)
                3. Clean catalog
                4. Save artworks
                5. Save seasons
                6. Save medias
             */
            coordinator.setTotalSteps(folders.size + newFiles.size + 4)

            var catalog = getCatalog(files = newFiles)
            catalog = applyCurrentMediaProgress(catalog, dbMedias = dbMedias)

            if (onlyNew) database.deleteMediasNotInFiles((deviceFiles + existingFiles).distinct()) else database.deleteAll()
            coordinator.incrementProgress()

            database.saveArtworks(catalog.artworks); coordinator.incrementProgress()
            database.saveSeasons(catalog.seasons); coordinator.incrementProgress()
            database.saveMedias(catalog.movies + catalog.episodes); coordinator.incrementProgress()

            // TODO: Delete in October 2026
            database.updateRealPaths(files = deviceFiles)

            imagesPrefetchManager.prefetchImages()
            user.setSyncTime(System.currentTimeMillis())
            user.setVersionCode(appInfo.versionCode)

        }

    }

    private suspend fun getCatalog(files: List<UserFile>) : Catalog {

        val folders = files.groupInFolders()

        // Get artworks
        val artworkFiles = artworkFolderFetcher.fetch(
            folders = folders,
            onProgress = { coordinator.incrementProgress() }
        )

        val (movies, seasonsAndTmdbEpisodes) = supervisorScope {
            val moviesDeferred = async {
                runCatching { movieMetadataFetcher.fetch(artworkFiles = artworkFiles, onProgress = { coordinator.incrementProgress() }) }
                    .onFailure { Trace.error(TAG, "getMovies failed", it) }
                    .getOrElse { emptyList() }
            }
            val seasonsAndTmdbEpisodesDeferred = async {
                runCatching { seasonMetadataFetcher.fetch(artworkFiles = artworkFiles) }
                    .onFailure { Trace.error(TAG, "getSeasons failed", it) }
                    .getOrElse { emptyList() }
            }

            moviesDeferred.await() to seasonsAndTmdbEpisodesDeferred.await()
        }

        val seasons = seasonsAndTmdbEpisodes.map { it.first }
        val tmdbEpisodes = seasonsAndTmdbEpisodes.flatMap { it.second }

        val episodes = episodeResolver.resolve(
            artworkFiles = artworkFiles,
            episodesDto = tmdbEpisodes,
            onProgress = { coordinator.incrementProgress() }
        )

        return Catalog(
            artworks = artworkFiles.map { it.artwork },
            movies = movies.filterIsInstance<Movie>(),
            seasons = seasons,
            episodes = episodes + movies.filterIsInstance<Episode>()
        )

    }

    /**
     * Copies watch status and current time from existing database media to matched new items.
     */
    private fun applyCurrentMediaProgress(catalog: Catalog, dbMedias: List<Media>) : Catalog {

        var count = 0

        val movies = catalog.movies.map { newMovie ->

            dbMedias.filterIsInstance<Movie>().find { it.file.name == newMovie.file.name && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldMovie ->

                count++

                newMovie.copy(
                    currentTime = oldMovie.currentTime,
                    status = oldMovie.status
                )

            } ?: newMovie

        }

        val episodes = catalog.episodes.map { newEpisode ->

            dbMedias.filterIsInstance<Episode>().find { it.file.name == newEpisode.file.name && (it.currentTime != 0L || it.status != Status.TO_WATCH) }?.let { oldEpisode ->

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

}