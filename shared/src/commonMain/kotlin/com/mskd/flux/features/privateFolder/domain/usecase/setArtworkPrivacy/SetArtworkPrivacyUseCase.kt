package com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.history.domain.repository.HistoryRepository

class SetArtworkPrivacyUseCase(
    private val database: DatabaseRepository,
    private val history: HistoryRepository
) {

    suspend operator fun invoke(artworkId: Long, isPrivate: Boolean) {
        database.setArtworkPrivate(artworkId = artworkId, isPrivate = isPrivate)

        // A private artwork must not appear in history
        if (isPrivate)
            history.delete(artworkId = artworkId)
    }

}
