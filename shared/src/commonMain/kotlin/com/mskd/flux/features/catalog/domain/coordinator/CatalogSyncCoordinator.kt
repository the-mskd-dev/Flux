package com.mskd.flux.features.catalog.domain.coordinator

import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.utils.Trace
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
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

interface CatalogSyncCoordinator {
    val state: StateFlow<SyncState>
    val isBusy: Boolean
    fun launch(full: Boolean, block: suspend CoroutineScope.() -> Unit)
    fun setTotalSteps(steps: Int)
    fun incrementProgress()
}

class CatalogSyncCoordinatorImpl(
    private val scope: CoroutineScope
) : CatalogSyncCoordinator {

    private companion object {
        const val TAG = "CatalogSyncCoordinator"
    }

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    override val state: StateFlow<SyncState> = _state.asStateFlow()

    private var activeJob: Job? = null
    private val completedSteps = AtomicInteger(0)
    private var totalSteps = 1

    override val isBusy: Boolean get() = _state.value is SyncState.Syncing

    @OptIn(InternalCoroutinesApi::class)
    override fun launch(full: Boolean, block: suspend CoroutineScope.() -> Unit) {
        activeJob?.cancel()
        completedSteps.set(0)
        totalSteps = 1
        _state.value = SyncState.Syncing(full = full)

        activeJob = scope.launch {
            block()
            _state.value = SyncState.Idle
        }

    }

    override fun setTotalSteps(steps: Int) {
        totalSteps = steps.coerceAtLeast(1)

        Trace.info(tag = TAG, message = "Set up for $steps steps")
    }

    override fun incrementProgress() {
        val completed = completedSteps.incrementAndGet().toFloat()
        val progress = (completed / totalSteps).coerceIn(0f, 1f)
        _state.update { current ->
            if (current is SyncState.Syncing) {
                current.copy(
                    progress = progress,
                    description = StringProvider.Resource(Res.string.sync_in_progress)
                )
            } else {
                current
            }
        }
        Trace.info(tag = TAG, message = "Progress: ${completed.roundToInt()}/$totalSteps (${progress.times(100).roundToInt()}%)")
    }

}