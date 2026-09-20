package com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore

class EnablePrivateFolderUseCase(
    private val privateFolderDataStore: PrivateFolderDataStore
) {

    suspend operator fun invoke(pin: String) {
        privateFolderDataStore.setPin(pin = pin)
        privateFolderDataStore.setEnabled(enabled = true)
    }

}
