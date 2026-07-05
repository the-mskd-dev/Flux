package com.mskd.flux.features.artwork.domain

import com.mskd.flux.features.artwork.domain.usecase.getArtwork.GetArtworkUseCase
import com.mskd.flux.features.artwork.domain.usecase.getArtwork.GetArtworkUseCaseImpl
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCaseImpl
import com.mskd.flux.features.artwork.domain.usecase.saveArtwork.SaveArtworkUseCase
import com.mskd.flux.features.artwork.domain.usecase.saveArtwork.SaveArtworkUseCaseImpl
import org.koin.dsl.module

val moduleArtwork = module {

    single<GetArtworkUseCase> {
        GetArtworkUseCaseImpl(
            database = get()
        )
    }

    single<ObserveArtworkUseCase> {
        ObserveArtworkUseCaseImpl(
            database = get()
        )
    }

    single<SaveArtworkUseCase> {
        SaveArtworkUseCaseImpl(
            database = get()
        )
    }

}