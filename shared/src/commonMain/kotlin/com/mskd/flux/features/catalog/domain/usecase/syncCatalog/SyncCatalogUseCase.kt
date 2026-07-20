package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import com.mskd.flux.features.catalog.domain.model.SyncState
import kotlinx.coroutines.flow.StateFlow

interface SyncCatalogUseCase {
    val state: StateFlow<SyncState>
    operator fun invoke(onlyNew: Boolean)
}