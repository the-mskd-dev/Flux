package com.mskd.flux.features.history.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.history.domain.repository.HistoryRepository

class SaveToHistoryUseCase(
    private val repository: HistoryRepository,
    private val artworkRepository: DatabaseRepository
) {

    suspend operator fun invoke(media: Media) {

    }

}