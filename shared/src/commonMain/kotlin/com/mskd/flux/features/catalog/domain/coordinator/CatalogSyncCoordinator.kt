package com.mskd.flux.features.catalog.domain.coordinator

import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.report.Trace
import com.mskd.flux.ui.text.TextProvider
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.sync_in_progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

interface CatalogSyncCoordinator {
    val state: StateFlow<SyncState>
    val isBusy: Boolean
    fun launch(full: Boolean, block: suspend CoroutineScope.() -> Unit)
    fun setFailedFiles(count: Int)
    fun setProgress(progress: Float)
}

class CatalogSyncCoordinatorImpl(
    private val scope: CoroutineScope
) : CatalogSyncCoordinator {

    private companion object {
        const val TAG = "CatalogSyncCoordinator"
    }

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle())
    override val state: StateFlow<SyncState> = _state.asStateFlow()

    private var activeJob: Job? = null
    private var failedFiles = 0

    override val isBusy: Boolean get() = _state.value is SyncState.Syncing

    @OptIn(InternalCoroutinesApi::class)
    override fun launch(full: Boolean, block: suspend CoroutineScope.() -> Unit) {
        activeJob?.cancel()
        failedFiles = 0
        _state.value = SyncState.Syncing(full = full)

        activeJob = scope.launch {
            block()
            _state.value = SyncState.Idle(failedFiles = failedFiles)
        }

    }

    override fun setFailedFiles(count: Int) {
        failedFiles = count
    }

    override fun setProgress(progress: Float) {
        _state.update { current ->
            if (current is SyncState.Syncing) {
                current.copy(
                    progress = progress.coerceIn(0f, 1f),
                    description = TextProvider.Resource(Res.string.sync_in_progress)
                )
            } else {
                current
            }
        }
        Trace.info(tag = TAG, message = "Progress: ${progress.times(100).roundToInt()}%")
    }

}
