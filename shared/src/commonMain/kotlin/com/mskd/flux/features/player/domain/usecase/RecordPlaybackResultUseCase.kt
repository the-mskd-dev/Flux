package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.history.domain.usecase.SaveToHistoryUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase

class RecordPlaybackResultUseCase(
    private val saveProgress: SaveProgressUseCase,
    private val saveToHistory: SaveToHistoryUseCase
) {
    suspend operator fun invoke(media: Media, progress: Long): Media {
        val updatedMedia = saveProgress(media = media, progress = progress)
        saveToHistory(media = updatedMedia)
        return updatedMedia
    }
}