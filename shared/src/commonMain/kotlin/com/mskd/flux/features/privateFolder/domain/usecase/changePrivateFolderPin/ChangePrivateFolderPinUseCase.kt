package com.mskd.flux.features.privateFolder.domain.usecase.changePrivateFolderPin

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore

class ChangePrivateFolderPinUseCase(
    private val privateFolderDataStore: PrivateFolderDataStore
) {

    suspend operator fun invoke(oldPin: String, newPin: String): Boolean {
        val pinIsValid = privateFolderDataStore.verifyPin(pin = oldPin)

        if (pinIsValid)
            privateFolderDataStore.setPin(pin = newPin)

        return pinIsValid
    }

}
