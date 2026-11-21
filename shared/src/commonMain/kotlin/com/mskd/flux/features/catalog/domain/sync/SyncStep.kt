package com.mskd.flux.features.catalog.domain.sync

/**
 * Sync step: a global treatment of the Sync. It declares its work, runs it and signals its progress
 * through [SyncContext.progress], one completed unit at a time.
 */
interface SyncStep {

    /** Whether the step runs in this Sync. */
    fun isEnabled(context: SyncContext): Boolean = true

    /** Estimated units of work, deduced from the file names or the Catalog. Called before any remaining Sync step runs (after the scan step when enabled). */
    suspend fun units(context: SyncContext): Int

    suspend fun run(context: SyncContext)

}
