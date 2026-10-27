package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.fetcher.CatalogContentFetcher
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.files.domain.usecase.ScanSourcesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.people.domain.usecase.SyncPeopleUseCase
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

class SyncCatalogUseCase(
    private val database: DatabaseRepository,
    private val privateFolder: PrivateFolderDataStore,
    private val user: UserDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val appInfo: AppInfo,
    private val coordinator: CatalogSyncCoordinator,
    private val scanSourcesUseCase: ScanSourcesUseCase,
    private val syncGenresUseCase: SyncGenresUseCase,
    private val catalogFetcher: CatalogContentFetcher,
    private val syncPeopleUseCase: SyncPeopleUseCase,
) {

    companion object { const val TAG = "SyncCatalogUseCase" }

    val state: StateFlow<SyncState> = coordinator.state

    operator fun invoke(onlyNew: Boolean) {

        if (coordinator.isBusy && (coordinator.state.value as? SyncState.Syncing)?.full == true && onlyNew)
            return

        coordinator.launch(full = !onlyNew) {

            val dbMedias = database.getMedias()
            val scan = scanSourcesUseCase()
            val deviceFiles = scan.files

            // Fichiers manquants: removed right after the scan, whatever happens next
            database.deleteMediasNotInFiles(scan.existingFiles(knownFiles = dbMedias.map { it.file }))

            val newFiles = if (!onlyNew) deviceFiles else {
                deviceFiles.filter { file -> dbMedias.none { it.file.path == file.path && it.file.name == file.name } }
            }

            if (newFiles.isEmpty()) {
                user.setSyncTime(Clock.System.now().toEpochMilliseconds())
                user.setVersionCode(appInfo.versionCode)
                syncPeopleUseCase(force = !onlyNew)
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
                .distinctBy { (it.nameProperties.tmdbId ?: it.nameProperties.title) to it.season }
                .size

            // Medias
            steps += newFiles.size

            // Save
            steps += 1

            coordinator.setTotalSteps(steps)

            // Get Genres
            if (!onlyNew) syncGenresUseCase()
            coordinator.incrementProgress()

            val catalog = catalogFetcher.fetch(
                folders = folders,
                onProgress = { coordinator.incrementProgress() }
            )

            coordinator.setFailedFiles(catalog.failedFiles.size)

            // Upsert: nothing is wiped, private flags and user state (by file path) are kept by the save
            database.saveArtworks(catalog.artworks)
            database.saveSeasons(catalog.seasons)
            // The primary key of medias is (id, artworkId): never save twice the same media identity
            database.saveSyncedMedias((catalog.movies + catalog.episodes).distinctBy { it.mediaId to it.artworkId })

            // Move NSFW artworks to the private folder when included
            val privateFolderState = privateFolder.flow.first()

            if (privateFolderState.enabled && privateFolderState.includeNsfw)
                database.setNsfwArtworksPrivate()

            // Background cast sync: only new artworks on light sync, everything on full sync
            if (onlyNew) syncPeopleUseCase(artworks = catalog.artworks) else syncPeopleUseCase(force = true)

            coordinator.incrementProgress()

            imagesPrefetchManager.prefetchImages()
            user.setSyncTime(Clock.System.now().toEpochMilliseconds())
            user.setVersionCode(appInfo.versionCode)

        }

    }

}