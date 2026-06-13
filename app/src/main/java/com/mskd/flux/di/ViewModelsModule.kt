package com.mskd.flux.di

import com.mskd.flux.MainViewModel
import com.mskd.flux.screens.artwork.ArtworkViewModel
import com.mskd.flux.screens.customization.CustomizationViewModel
import com.mskd.flux.screens.home.HomeViewModel
import com.mskd.flux.screens.settings.SettingsViewModel
import com.mskd.flux.screens.unknown.UnknownViewModel
import com.mskd.flux.screens.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.viewModel

val viewModelsModule = module {

    viewModel<MainViewModel>()
    viewModel<HomeViewModel>()
    viewModel<UnknownViewModel>()
    viewModel<SettingsViewModel>()
    viewModel<WelcomeViewModel>()
    viewModel<CustomizationViewModel>()

    viewModel { params ->
        ArtworkViewModel(
            artworkId = params.get(),
            season = params.getOrNull(),
            artworkUC = get(),
            settingsRepository = get(),
            progressUC = get(),
        )
    }

}