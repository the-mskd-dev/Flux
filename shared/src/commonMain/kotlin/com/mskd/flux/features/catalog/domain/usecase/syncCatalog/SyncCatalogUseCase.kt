package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.fetcher.CatalogContentFetcher
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.UpdateGenreIdsUseCase
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.flow.StateFlow

class SyncCatalogUseCase(
    private val database: DatabaseRepository,
    private val detailsRepository: DetailsRepository,
    private val user: UserDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val appInfo: AppInfo,
    private val coordinator: CatalogSyncCoordinator,
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val filterExistingFilesUseCase: FilterExistingFilesUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val updateGenreIdsUseCase: UpdateGenreIdsUseCase,
    private val catalogFetcher: CatalogContentFetcher,
) {

    companion object { const val TAG = "SyncCatalogUseCase" }

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

            // TODO: Delete in October 2026
            val genreSyncNeeded = detailsRepository.getGenresCount() == 0

            if (newFiles.isEmpty()) {
                database.deleteMediasNotInFiles(existingFiles)

                // TODO: Delete in October 2026
                if (genreSyncNeeded) {
                    updateGenreIdsUseCase(
                        onProgressCount = { coordinator.setTotalSteps(it + 1) },
                        onProgress = { coordinator.incrementProgress() }
                    )
                    syncGenresUseCase()
                    coordinator.incrementProgress()
                }

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

            // TODO: Delete in October 2026 and move setTotalSteps outside
            updateGenreIdsUseCase(
                onProgressCount = { coordinator.setTotalSteps(it + steps) },
                onProgress = { coordinator.incrementProgress() }
            )

            //coordinator.setTotalSteps(steps)

            // Get Genres
            if (!onlyNew || genreSyncNeeded) syncGenresUseCase()
            coordinator.incrementProgress()

            var catalog = catalogFetcher.fetch(
                folders = folders,
                onProgress = { coordinator.incrementProgress() }
            )
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

}