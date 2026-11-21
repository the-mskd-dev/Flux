package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.model.SyncKind
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import kotlin.time.Clock

/** Saves the Sync time and version code for file-scanning Syncs. */
class FinalizationStep(
    private val user: UserDataStore,
    private val appInfo: AppInfo,
) : SyncStep {

    override fun isEnabled(context: SyncContext) = context.kind != SyncKind.LANGUAGE_CHANGE

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {

        user.setSyncTime(Clock.System.now().toEpochMilliseconds())
        user.setVersionCode(appInfo.versionCode)

        context.progress.complete()

    }

}
