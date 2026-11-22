package com.mskd.flux.features.catalog.domain.sync

import com.mskd.flux.features.catalog.domain.model.SyncType

/**
 * Sync step: a global treatment of the Sync. It declares its work, runs it and signals its progress
 * through [SyncContext.progress], one completed unit at a time.
 */
interface SyncStep {

    /** Sync types in which this step runs. */
    val syncTypes: Set<SyncType>

    /** Estimated units of work, deduced from the file names or the Catalog. Called before any remaining Sync step runs (after the scan step when enabled). */
    suspend fun units(context: SyncContext): Int

    suspend fun run(context: SyncContext)

}
