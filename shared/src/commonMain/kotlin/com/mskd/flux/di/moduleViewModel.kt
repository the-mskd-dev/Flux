package com.mskd.flux.di

import com.mskd.flux.screen.artwork.ArtworkViewModel
import com.mskd.flux.screen.customization.CustomizationViewModel
import com.mskd.flux.screen.home.HomeViewModel
import com.mskd.flux.screen.search.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
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

    viewModelOf(::CustomizationViewModel)

    viewModelOf(::HomeViewModel)

    viewModel { params ->
        SearchViewModel(
            contentType = params.getOrNull(),
            catalogUC = get(),
            settingsRepository = get()
        )
    }

}