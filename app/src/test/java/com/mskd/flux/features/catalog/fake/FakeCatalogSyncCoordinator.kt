package com.mskd.flux.features.catalog.fake

import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FakeCatalogSyncCoordinator(
    private val scope: CoroutineScope,
    initialState: SyncState = SyncState.Idle()
) : CatalogSyncCoordinator {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<SyncState> = _state

    override val isBusy: Boolean
        get() = _state.value is SyncState.Syncing

    var launchCallCount = 0
        private set
    var lastFull: Boolean? = null
        private set
    var job: Job? = null
        private set

    override fun launch(full: Boolean, block: suspend CoroutineScope.() -> Unit) {
        launchCallCount++
        lastFull = full
        // Like the real coordinator, a new Synchro cancels the running one
        job?.cancel()
        _state.update { SyncState.Syncing(full = full) }
        job = scope.launch {
            block()
            _state.update { SyncState.Idle(failedFiles = failedFiles) }
        }
    }

    var failedFiles = 0
        private set

    override fun setFailedFiles(count: Int) { failedFiles = count }
    override fun setProgress(progress: Float) {
        _state.update { if (it is SyncState.Syncing) it.copy(progress = progress) else it }
    }
}