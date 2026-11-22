package com.mskd.flux.features.catalog.domain.sync.step

import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncStep
import com.mskd.flux.features.images.domain.usecase.PrefetchImagesUseCase

/** Runs the Image prefetch in the background at the end of a Sync without waiting for it. */
class PrefetchImagesStep(
    private val prefetchImagesUseCase: PrefetchImagesUseCase,
) : SyncStep {

    override val syncTypes = setOf(SyncType.LIGHT, SyncType.FULL, SyncType.LANGUAGE_CHANGE)

    override suspend fun units(context: SyncContext) = 1

    override suspend fun run(context: SyncContext) {
        prefetchImagesUseCase()
        context.progress.complete()
    }

}
