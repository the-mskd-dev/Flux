package com.mskd.flux.di

import com.mskd.flux.features.token.presentation.TokenViewModel
import com.mskd.flux.features.catalog.presentation.search.SearchViewModel
import com.mskd.flux.screen.settings.SettingsViewModel
import com.mskd.flux.screen.show.ShowViewModel
import com.mskd.flux.screen.unknown.UnknownViewModel
import com.mskd.flux.screen.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleViewModel = module {

    viewModelOf(::SettingsViewModel)

    viewModel { params ->
        ShowViewModel(
            artworkId = params.get(),
            observeArtworkUseCase = get(),
            resetProgress = get(),
        )
    }

    viewModel { params ->
        TokenViewModel(
            fromSettings = params.get(),
            tokenDataStore = get(),
            saveTokenAndSyncUseCase = get(),
            appInfo = get()
        )
    }

    viewModelOf(::UnknownViewModel)

    viewModelOf(::WelcomeViewModel)

}