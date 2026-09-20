package com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.markNsfwArtworksPrivate.MarkNsfwArtworksPrivateUseCase
import kotlinx.coroutines.flow.first

class EnablePrivateFolderUseCase(
    private val privateFolderDataStore: PrivateFolderDataStore,
    private val markNsfwArtworksPrivate: MarkNsfwArtworksPrivateUseCase
) {

    suspend operator fun invoke(pin: String) {
        privateFolderDataStore.setPin(pin = pin)
        privateFolderDataStore.setEnabled(enabled = true)

        // When enabled, NSFW artworks are moved to the private folder if included
        val includeNsfw = privateFolderDataStore.flow.first().includeNsfw

        if (includeNsfw)
            markNsfwArtworksPrivate()
    }

}
