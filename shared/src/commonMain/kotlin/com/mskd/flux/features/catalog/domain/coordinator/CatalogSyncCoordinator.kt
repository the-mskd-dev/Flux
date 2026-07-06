package com.mskd.flux.features.catalog.domain.coordinator

import com.mskd.flux.features.catalog.domain.model.SyncState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class CatalogSyncCoordinator(
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private var activeJob: Job? = null
    private val completedSteps = AtomicInteger(0)
    private var totalSteps = 1

    val isBusy: Boolean get() = _state.value is SyncState.Syncing

    @OptIn(InternalCoroutinesApi::class)
    fun launch(full: Boolean, block: suspend CoroutineScope.() -> Unit) {
        activeJob?.cancel()
        completedSteps.set(0)
        totalSteps = 1
        _state.value = SyncState.Syncing(full = full)

        activeJob = scope.launch {
            block()
            _state.value = SyncState.Idle
        }

    }

    fun setTotalSteps(steps: Int) {
        totalSteps = steps.coerceAtLeast(1)
    }

    fun incrementProgress() {
        val completed = completedSteps.incrementAndGet().toFloat()
        val progress = (completed / totalSteps).coerceIn(0f, 1f)
        _state.update { current ->
            if (current is SyncState.Syncing) current.copy(progress = progress) else current
        }
    }

}