package com.mskd.flux.features.catalog.domain.sync

import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Progression de synchro: units done / units declared, all with the same weight (≈ one call).
 *
 * Steps and Enrichissements [declare] their estimated work and may [declare] more along the way. The progress
 * never goes backwards and stays under 100 % until [finish].
 */
class SyncProgress(private val coordinator: CatalogSyncCoordinator) {

    private val lock = Mutex()
    private var declared = 0
    private var done = 0
    private var last = 0f

    suspend fun declare(units: Int) = update { declared += units }

    suspend fun complete(units: Int = 1) = update { done += units }

    suspend fun finish() = lock.withLock { publish(1f) }

    private suspend fun update(change: () -> Unit) = lock.withLock {
        change()
        // The estimates can be too low: the work done then widens the declared total
        if (done > declared) declared = done
        if (declared > 0) publish((done.toFloat() / declared).coerceAtMost(MAX_BEFORE_END))
    }

    private fun publish(progress: Float) {
        if (progress <= last) return
        last = progress
        coordinator.setProgress(progress)
    }

    private companion object { const val MAX_BEFORE_END = 0.99f }

}
