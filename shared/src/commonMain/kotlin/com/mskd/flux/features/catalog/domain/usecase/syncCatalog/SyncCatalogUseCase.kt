package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.catalog.Catalog
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcher
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.resolver.MediaResolver
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.utils.Trace
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.supervisorScope

class SyncCatalogUseCase(
    private val database: DatabaseRepository,
    private val user: UserDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val appInfo: AppInfo,
    private val coordinator: CatalogSyncCoordinator,
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val filterExistingFilesUseCase: FilterExistingFilesUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val artworkMetadataFetcher: ArtworkMetadataFetcher,
    private val movieMetadataFetcher: MovieMetadataFetcher,
    private val seasonMetadataFetcher: SeasonMetadataFetcher,
    private val mediaResolver: MediaResolver
) {

    private companion object { const val TAG = "SyncCatalogUseCase" }

    val state: StateFlow<SyncState> = coordinator.state

    operator fun invoke(onlyNew: Boolean) {

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

            // Genres
            var steps = 1

            // Artworks
            steps += folders.size

            // Seasons
            steps += newFiles
                .filter { it.season != null }
                .distinctBy { it.nameProperties.title to it.season }
                .size

            // Medias
            steps += newFiles.size

            // Save
            steps += 1

            // Update real paths
            steps += 1

            coordinator.setTotalSteps(steps)

            // Get Genres
            if (!onlyNew) syncGenresUseCase()
            coordinator.incrementProgress()

            var catalog = getCatalog(folders = folders)
            catalog = applyCurrentMediaProgress(catalog, dbMedias = dbMedias)

            // Save new content
            if (onlyNew) database.deleteMediasNotInFiles((deviceFiles + existingFiles).distinct()) else database.deleteAll()
            database.saveArtworks(catalog.artworks)
            database.saveSeasons(catalog.seasons)
            database.saveMedias(catalog.movies + catalog.episodes)
            coordinator.incrementProgress()

            // TODO: Delete in October 2026
            database.updateRealPaths(files = deviceFiles)
            coordinator.incrementProgress()

            imagesPrefetchManager.prefetchImages()
            user.setSyncTime(System.currentTimeMillis())
            user.setVersionCode(appInfo.versionCode)

        }

    }

    private suspend fun getCatalog(folders: List<CatalogFolder>) : Catalog {

        // Get artworks
        val artworkFiles = artworkMetadataFetcher.fetch(
            folders = folders,
            onProgress = { coordinator.incrementProgress() }
        )

        // Get movies, seasons and episodes
        val (movies, seasonsAndEpisodes) = supervisorScope {
            val moviesDeferred = async {
                runCatching { movieMetadataFetcher.fetch(artworkWithFiles = artworkFiles, onProgress = { coordinator.incrementProgress() }) }
                    .onFailure { Trace.error(TAG, "getMovies failed", it) }
                    .getOrElse { emptyList() }
            }
            val seasonsAndEpisodesDeferred = async {
                runCatching { seasonMetadataFetcher.fetch(artworkWithFiles = artworkFiles, onProgress = { coordinator.incrementProgress() }) }
                    .onFailure { Trace.error(TAG, "getSeasons failed", it) }
                    .getOrElse { emptyList() }
            }

            moviesDeferred.await() to seasonsAndEpisodesDeferred.await()
        }

        // Create unknown medias
        val unknownMedias = artworkFiles.filter { it.artwork.isUnknown }.flatMap {
            it.files.map { file -> Episode(file) }
        }

        // Resolve translation and duration for medias
        val medias = mediaResolver.resolve(
            medias = movies + seasonsAndEpisodes.flatMap { it.second } + unknownMedias,
            onProgress = { coordinator.incrementProgress() }
        )

        return Catalog(
            artworks = artworkFiles.map { it.artwork },
            movies = medias.filterIsInstance<Movie>(),
            seasons = seasonsAndEpisodes.map { it.first },
            episodes = medias.filterIsInstance<Episode>()
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