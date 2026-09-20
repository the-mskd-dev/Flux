package com.mskd.flux.features.privateFolder.domain.usecase.disablePrivateFolder

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore

class DisablePrivateFolderUseCase(
    private val privateFolderDataStore: PrivateFolderDataStore,
    private val database: DatabaseRepository
) {

    suspend operator fun invoke(pin: String): Boolean {
        val pinIsValid = privateFolderDataStore.verifyPin(pin = pin)

        if (!pinIsValid) return false

        // Disabling the private folder makes every private artwork public again
        database.clearPrivateArtworks()
        privateFolderDataStore.setEnabled(enabled = false)

        return true
    }

}
