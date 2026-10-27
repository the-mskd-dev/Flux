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

        // The credits of the Artworks fetched by this Synchro are already saved with them: only the periodic refresh remains
        if (context.filesToFetch.isEmpty()) syncPeopleUseCase(force = false)

        imagesPrefetchManager.prefetchImages()

        context.progress.complete()

    }

}
