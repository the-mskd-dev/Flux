package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.core.model.core.StringProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.loading_your_catalog

sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(
        val full: Boolean,
        val progress: Float = 0f,
        val description: StringProvider = StringProvider.Resource(Res.string.loading_your_catalog)
    ) : SyncState()
}