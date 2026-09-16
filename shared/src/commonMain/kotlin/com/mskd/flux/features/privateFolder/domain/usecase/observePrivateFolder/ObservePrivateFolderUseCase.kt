package com.mskd.flux.features.privateFolder.domain.usecase.observePrivateFolder

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import kotlinx.coroutines.flow.Flow

interface ObservePrivateFolderUseCase {
    val flow: Flow<PrivateFolderDataStore.State>
}
