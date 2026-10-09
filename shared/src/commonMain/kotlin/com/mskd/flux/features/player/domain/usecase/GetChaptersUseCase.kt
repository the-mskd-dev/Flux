package com.mskd.flux.features.player.domain.usecase

import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.player.domain.model.VideoChapter

interface GetChaptersUseCase {
    suspend operator fun invoke(media: Media): List<VideoChapter>
}