package com.mskd.flux.features.privateFolder.domain.usecase.observePrivateFolder

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import kotlinx.coroutines.flow.Flow

class ObservePrivateFolderUseCaseImpl(
    privateFolderDataStore: PrivateFolderDataStore
) : ObservePrivateFolderUseCase {

    override val flow: Flow<PrivateFolderDataStore.State> = privateFolderDataStore.flow

}
