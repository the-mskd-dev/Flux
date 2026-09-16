package com.mskd.flux.features.privateFolder.domain.usecase.disablePrivateFolder

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore

class DisablePrivateFolderUseCase(
    private val privateFolderDataStore: PrivateFolderDataStore
) {

    suspend operator fun invoke(pin: String): Boolean {
        val pinIsValid = privateFolderDataStore.verifyPin(pin = pin)

        if (pinIsValid)
            privateFolderDataStore.setEnabled(enabled = false)

        return pinIsValid
    }

}
