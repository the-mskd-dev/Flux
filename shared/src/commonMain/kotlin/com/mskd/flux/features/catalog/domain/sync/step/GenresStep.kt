package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase

/** Refreshes the genres, in a Synchro complète only. */
class GenresStep(private val syncGenresUseCase: SyncGenresUseCase) : SyncStep {

    override fun isEnabled(context: SyncContext) = context.full

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {
        syncGenresUseCase()
        context.progress.complete()
    }

}
