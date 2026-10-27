package com.mskd.flux.features.catalog.domain.sync

/**
 * Étape de synchro: a global treatment of the Synchro. It declares its work, runs it and signals its progress
 * through [SyncContext.progress], one completed unit at a time.
 */
interface SyncStep {

    /** Whether the step runs in this Synchro. */
    fun isEnabled(context: SyncContext): Boolean = true

    /** Estimated units of work, deduced from the file names. Called once the scan is done, before any step runs. */
    fun units(context: SyncContext): Int

    suspend fun run(context: SyncContext)

}
