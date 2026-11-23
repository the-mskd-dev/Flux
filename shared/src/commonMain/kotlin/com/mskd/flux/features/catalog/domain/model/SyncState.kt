package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.ui.text.TextProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.loading_your_catalog

sealed class SyncState {
    /** [failedFiles]: Failed files of the last Sync, retried by the next Light sync. */
    data class Idle(val failedFiles: Int = 0) : SyncState()
    data class Syncing(
        val type: SyncType,
        val progress: Float = 0f,
        val description: TextProvider = TextProvider.Resource(Res.string.loading_your_catalog)
    ) : SyncState()
}