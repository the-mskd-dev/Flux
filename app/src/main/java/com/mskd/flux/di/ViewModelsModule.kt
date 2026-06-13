package com.mskd.flux.di

import com.mskd.flux.MainViewModel
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.screens.artwork.ArtworkViewModel
import com.mskd.flux.screens.customization.CustomizationViewModel
import com.mskd.flux.screens.home.HomeViewModel
import com.mskd.flux.screens.player.PlayerViewModel
import com.mskd.flux.screens.player.controllers.PlayerManager
import com.mskd.flux.screens.search.SearchViewModel
import com.mskd.flux.screens.settings.SettingsViewModel
import com.mskd.flux.screens.show.ShowViewModel
import com.mskd.flux.screens.unknown.UnknownViewModel
import com.mskd.flux.screens.welcome.WelcomeViewModel
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.progress.ProgressUC
import dagger.assisted.Assisted
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
        SearchViewModel(
            contentType = params.getOrNull(),
            catalogUC = get(),
            settingsRepository = get()
        )
    }

    viewModel { params ->
        ShowViewModel(
            artworkId = params.get(),
            artworkUC = get(),
            settingsRepository = get(),
            progressUC = get(),
        )
    }

    viewModel { params ->
        ArtworkViewModel(
            artworkId = params.get(),
            season = params.getOrNull(),
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
        )
    }

}