package com.mskd.flux.di

import com.mskd.flux.MainViewModel
import com.mskd.flux.screens.player.PlayerViewModel
import com.mskd.flux.screen.search.SearchViewModel
import com.mskd.flux.screens.settings.SettingsViewModel
import com.mskd.flux.screens.show.ShowViewModel
import com.mskd.flux.screens.token.TokenViewModel
import com.mskd.flux.screens.unknown.UnknownViewModel
import com.mskd.flux.screens.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleViewModelAndroid = module {

    viewModelOf(::MainViewModel)
    viewModelOf(::UnknownViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::WelcomeViewModel)

    viewModel { params ->
        ShowViewModel(
            artworkId = params.get(),
            artworkUC = get(),
            settingsRepository = get(),
            progressUC = get(),
        )
    }

    viewModel { params ->
        PlayerViewModel(
            mediaId = params.get(),
            artworkUC = get(),
            settingsRepository = get(),
            filesRepository = get(),
            playerManager = get(),
            progressUC = get(),
            pipIsEnabledUC = get()
        )
    }

    viewModel { params ->
        TokenViewModel(
            fromSettings = params.get(),
            tokenRepository = get(),
            tmdbService = get(),
            catalogUC = get(),
            appInfo = get()
        )
    }

}