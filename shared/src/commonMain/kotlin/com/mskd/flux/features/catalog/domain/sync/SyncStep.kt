package com.mskd.flux.features.catalog.domain.sync

/**
 * Sync step: a global treatment of the Sync. It declares its work, runs it and signals its progress
 * through [SyncContext.progress], one completed unit at a time.
 */
interface SyncStep {

    /** Whether the step runs in this Sync. */
    fun isEnabled(context: SyncContext): Boolean = true

    /** Estimated units of work, deduced from the file names or the Catalog. Called once the scan is done, before any step runs. */
    suspend fun units(context: SyncContext): Int

    suspend fun run(context: SyncContext)

}
