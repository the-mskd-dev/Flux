package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.people.domain.usecase.SyncPeopleUseCase
import kotlin.time.Clock

/** Saves the Synchro time and version code, then starts the background work without waiting for it. */
class FinalizationStep(
    private val user: UserDataStore,
    private val appInfo: AppInfo,
    private val syncPeopleUseCase: SyncPeopleUseCase,
    private val imagesPrefetchManager: ImagesPrefetchManager,
) : SyncStep {

    override fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        user.setSyncTime(Clock.System.now().toEpochMilliseconds())
        user.setVersionCode(appInfo.versionCode)

        // Background cast sync: only new artworks on light sync, everything on full sync
        when {
            context.full -> syncPeopleUseCase(force = true)
            context.filesToFetch.isEmpty() -> syncPeopleUseCase(force = false)
            else -> syncPeopleUseCase(artworks = context.savedArtworks)
        }

        imagesPrefetchManager.prefetchImages()

        context.progress.complete()

    }

}
