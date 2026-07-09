package com.mskd.flux.features.artwork.domain.usecase.observeArtwork

import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.core.State
import kotlinx.coroutines.flow.Flow

interface ObserveArtworkUseCase {
    val flow: Flow<State<FullArtwork>>
    operator fun invoke(artworkId: Long)
}