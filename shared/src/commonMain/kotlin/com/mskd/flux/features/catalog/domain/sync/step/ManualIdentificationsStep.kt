package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep

/**
 * Clears every Identification manuelle before the catalogue step, in a Synchro complète only: every file is then
 * identified by name search again (a TMDB ID in the name still wins). Status and Progress are kept by the save, by path.
 */
class ManualIdentificationsStep(private val database: DatabaseRepository) : SyncStep {

    override fun isEnabled(context: SyncContext) = context.full

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {
        database.clearManualIdentifications()
        context.progress.complete()
    }

}
