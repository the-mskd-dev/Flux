package com.mskd.flux.features.artwork.domain.usecase.observeArtwork

import com.mskd.flux.core.domain.model.artwork.FullArtwork
import com.mskd.flux.core.domain.model.core.State
import kotlinx.coroutines.flow.Flow

interface ObserveArtworkUseCase {
    val flow: Flow<State<FullArtwork>>
    operator fun invoke(artworkId: Long)
}