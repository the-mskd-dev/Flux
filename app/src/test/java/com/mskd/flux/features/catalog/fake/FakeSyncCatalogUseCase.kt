package com.mskd.flux.features.catalog.fake

import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSyncCatalogUseCase : SyncCatalogUseCase {

    override val state: StateFlow<SyncState> = MutableStateFlow(SyncState.Idle)

    override fun invoke(onlyNew: Boolean) {}
}