package com.mskd.flux.features.privateFolder.domain.usecase.setIncludeNsfw

import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.markNsfwArtworksPrivate.MarkNsfwArtworksPrivateUseCase

class SetIncludeNsfwUseCase(
    private val privateFolderDataStore: PrivateFolderDataStore,
    private val markNsfwArtworksPrivate: MarkNsfwArtworksPrivateUseCase
) {

    suspend operator fun invoke(includeNsfw: Boolean) {
        privateFolderDataStore.setIncludeNsfw(includeNsfw = includeNsfw)

        // NSFW artworks are moved to the private folder when included
        if (includeNsfw)
            markNsfwArtworksPrivate()
    }

}
