package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import kotlin.time.Clock

/** Saves the Synchro time and version code, then starts the background work without waiting for it. */
class FinalizationStep(
    private val user: UserDataStore,
    private val appInfo: AppInfo,
    private val imagesPrefetchManager: ImagesPrefetchManager,
) : SyncStep {

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        user.setSyncTime(Clock.System.now().toEpochMilliseconds())
        user.setVersionCode(appInfo.versionCode)

        imagesPrefetchManager.prefetchImages()

        context.progress.complete()

    }

}
