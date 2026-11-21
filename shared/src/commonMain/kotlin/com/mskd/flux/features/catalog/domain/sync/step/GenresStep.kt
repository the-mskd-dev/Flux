package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.features.catalog.domain.model.SyncKind
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase

/** Refreshes the Genres in a Full sync and in a Language change. */
class GenresStep(private val syncGenresUseCase: SyncGenresUseCase) : SyncStep {

    override fun isEnabled(context: SyncContext) = context.kind == SyncKind.FULL || context.kind == SyncKind.LANGUAGE_CHANGE

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {
        syncGenresUseCase()
        context.progress.complete()
    }

}
