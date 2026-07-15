package com.mskd.flux.features.artwork

import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCase
import com.mskd.flux.features.artwork.domain.usecase.observeArtwork.ObserveArtworkUseCaseImpl
import com.mskd.flux.features.artwork.presentation.ArtworkViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleArtwork = module {

    single<ObserveArtworkUseCase> {
        ObserveArtworkUseCaseImpl(
            database = get(),
            sourcesUseCase = get()
        )
    }

    viewModel<ArtworkViewModel> { params ->
        ArtworkViewModel(
            artworkId = params.get(),
            season = params.getOrNull(),
            settingsDataStore = get(),
            changeMediaStatus = get(),
            markPreviousAsWatched = get(),
            resetProgress = get(),
            saveProgress = get(),
            observeArtworkUseCase = get()
        )
    }

}