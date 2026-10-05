package com.mskd.flux.features.catalog.domain.model

import com.mskd.flux.ui.text.TextProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.loading_your_catalog

sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(
        val full: Boolean,
        val progress: Float = 0f,
        val description: TextProvider = TextProvider.Resource(Res.string.loading_your_catalog)
    ) : SyncState()
}