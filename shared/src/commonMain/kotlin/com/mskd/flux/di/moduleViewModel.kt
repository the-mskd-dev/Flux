package com.mskd.flux.di

import com.mskd.flux.screen.artwork.ArtworkViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleViewModel = module {

    viewModel<ArtworkViewModel> { params ->
        ArtworkViewModel(
            artworkId = params.get(),
            season = params.getOrNull(),
            artworkUC = get(),
            settingsRepository = get(),
            progressUC = get(),
        )
    }

}