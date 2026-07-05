package com.mskd.flux.features.artwork.domain.usecase.getArtwork

import com.mskd.flux.core.domain.model.artwork.FullArtwork

interface GetArtworkUseCase {
    suspend operator fun invoke(artworkId: Long): FullArtwork?
}