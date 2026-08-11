package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.fetcher.CatalogContentFetcher
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.migration.LegacyGenresMigration
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.flow.StateFlow

class SyncCatalogUseCase(
    private val database: DatabaseRepository,
    private val user: UserDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val appInfo: AppInfo,
    private val coordinator: CatalogSyncCoordinator,
    private val getDeviceFilesUseCase: GetDeviceFilesUseCase,
    private val filterExistingFilesUseCase: FilterExistingFilesUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val legacyGenresMigration: LegacyGenresMigration,
    private val catalogFetcher: CatalogContentFetcher,
) {

    companion object { const val TAG = "SyncCatalogUseCase" }

    val state: StateFlow<SyncState> = coordinator.state

    operator fun invoke(onlyNew: Boolean) {

        if (coordinator.isBusy && (coordinator.state.value as? SyncState.Syncing)?.full == true && onlyNew)
            return

        coordinator.launch(full = !onlyNew) {

            val dbMedias = database.getMedias()
            val deviceFiles = getDeviceFilesUseCase()
            val existingFiles = filterExistingFilesUseCase(files = (dbMedias).map { it.file })

            // TODO: Delete in October 2026
            // Get old unknown files that haven't real path
            val unknownFiles = dbMedias.filter { it is Episode && it.isUnknown }
                .map { it.file }
                .filter { file -> existingFiles.any { it.path == file.path } && file.realPath.isEmpty() }

            // TODO: Delete in October 2026
            database.updateRealPaths(files = deviceFiles)

            val newFiles = if (!onlyNew) deviceFiles else {
                deviceFiles.filter { file -> existingFiles.none { it.path == file.path } } + unknownFiles
            }

            if (newFiles.isEmpty()) {
                database.deleteMediasNotInFiles(existingFiles)

                // TODO: Delete in October 2026
                val steps = legacyGenresMigration.getSteps()
                if (steps > 0) {
                    coordinator.setTotalSteps(steps)
                    legacyGenresMigration.migrate { coordinator.incrementProgress() }
                }

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

            steps += legacyGenresMigration.getSteps() // TODO: Delete in October 2026

            coordinator.setTotalSteps(steps)

            // TODO: Delete in October 2026
            legacyGenresMigration.migrate { coordinator.incrementProgress() }

            // Get Genres
            if (!onlyNew) syncGenresUseCase()
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

            imagesPrefetchManager.prefetchImages()
            user.setSyncTime(System.currentTimeMillis())
            user.setVersionCode(appInfo.versionCode)

        }

    }

}