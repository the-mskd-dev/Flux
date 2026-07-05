package com.mskd.flux.features.artwork.domain

import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCaseImpl
import org.koin.dsl.module

val moduleArtwork = module {

    single<ObserveArtworkUseCase> {
        ObserveArtworkUseCaseImpl(
            database = get()
        )
    }

}