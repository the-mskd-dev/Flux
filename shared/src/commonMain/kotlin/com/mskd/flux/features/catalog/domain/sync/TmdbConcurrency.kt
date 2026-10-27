package com.mskd.flux.features.catalog.domain.sync

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Bounds the TMDB calls in flight during a Synchro. A single instance is shared by all the Enrichissements:
 * it bounds the requests, which a dispatcher with limited parallelism does not (it only bounds the threads).
 */
class TmdbConcurrency {

    companion object {
        const val MAX_CALLS = 8
    }

    private val semaphore = Semaphore(MAX_CALLS)

    suspend fun <T> limit(call: suspend () -> T): T = semaphore.withPermit { call() }

}
