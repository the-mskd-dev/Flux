package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncKind
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.sync.SyncContext
import com.mskd.flux.features.catalog.domain.sync.SyncPipeline
import com.mskd.flux.features.catalog.domain.sync.SyncProgress
import kotlinx.coroutines.flow.StateFlow

class SyncCatalogUseCase(
    private val coordinator: CatalogSyncCoordinator,
    private val pipeline: SyncPipeline,
) {

    companion object { const val TAG = "SyncCatalogUseCase" }

    val state: StateFlow<SyncState> = coordinator.state

    operator fun invoke(kind: SyncKind) {

        if (coordinator.isBusy && (coordinator.state.value as? SyncState.Syncing)?.full == true && kind == SyncKind.LIGHT)
            return

        coordinator.launch(full = kind == SyncKind.FULL) {

            val context = SyncContext(kind = kind, progress = SyncProgress(coordinator))

            pipeline.run(context)

            coordinator.setFailedFiles(context.failedFiles)

        }

    }

}
