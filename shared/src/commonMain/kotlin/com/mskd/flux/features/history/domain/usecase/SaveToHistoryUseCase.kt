package com.mskd.flux.features.history.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.history.data.mapper.toHistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository

class SaveToHistoryUseCase(
    private val history: HistoryRepository,
    private val database: DatabaseRepository
) {

    suspend operator fun invoke(media: Media) {

        val entry = media.toHistoryEntry()

        when (media) {
            is Episode -> {

            }
            is Movie -> {

                if (media.status == Status.IS_WATCHING)
                    history.delete(artworkId = media.artworkId)
                else
                    history.insert(entry = entry)

            }
        }

    }

}