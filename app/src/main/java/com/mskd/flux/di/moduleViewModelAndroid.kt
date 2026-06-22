package com.mskd.flux.di

import com.mskd.flux.MainViewModel
import com.mskd.flux.screens.player.PlayerViewModel
import com.mskd.flux.screen.unknown.UnknownViewModel
import com.mskd.flux.screens.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleViewModelAndroid = module {

    viewModelOf(::MainViewModel)
    viewModelOf(::WelcomeViewModel)

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

}