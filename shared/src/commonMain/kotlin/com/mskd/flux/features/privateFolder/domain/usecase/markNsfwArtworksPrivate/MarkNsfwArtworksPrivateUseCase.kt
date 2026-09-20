package com.mskd.flux.features.privateFolder.domain.usecase.markNsfwArtworksPrivate

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.features.history.domain.repository.HistoryRepository

class MarkNsfwArtworksPrivateUseCase(
    private val database: DatabaseRepository,
    private val history: HistoryRepository
) {

    suspend operator fun invoke() {

        database.setNsfwArtworksPrivate()

        // A private artwork must not appear in history
        val nsfwArtworkIds = database.getNsfwArtworkIds()
        nsfwArtworkIds.forEach { history.delete(artworkId = it) }
    }

}
