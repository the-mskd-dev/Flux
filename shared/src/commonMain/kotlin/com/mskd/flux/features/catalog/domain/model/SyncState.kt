package com.mskd.flux.features.catalog.domain.model

sealed class SyncState {
    data object Idle : SyncState()
    data class Syncing(val full: Boolean, val progress: Float = 0f) : SyncState()
}