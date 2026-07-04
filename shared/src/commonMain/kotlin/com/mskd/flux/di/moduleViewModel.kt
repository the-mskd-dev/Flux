package com.mskd.flux.di

import com.mskd.flux.features.progress.domain.usecase.ChangeMediaStatusUseCase
import com.mskd.flux.features.progress.domain.usecase.MarkPreviousAsWatchedUseCase
import com.mskd.flux.features.progress.domain.usecase.ResetProgressUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.screen.artwork.ArtworkViewModel
import com.mskd.flux.screen.customization.CustomizationViewModel
import com.mskd.flux.screen.home.HomeViewModel
import com.mskd.flux.screen.search.SearchViewModel
import com.mskd.flux.screen.settings.SettingsViewModel
import com.mskd.flux.screen.show.ShowViewModel
import com.mskd.flux.screen.sources.SourcesViewModel
import com.mskd.flux.screen.token.TokenViewModel
import com.mskd.flux.screen.unknown.UnknownViewModel
import com.mskd.flux.screen.welcome.WelcomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleViewModel = module {

    viewModel<ArtworkViewModel> { params ->
        ArtworkViewModel(
            artworkId = params.get(),
            season = params.getOrNull(),
            artworkUC = get(),
            settingsDataStore = get(),
            changeMediaStatus = get(),
            markPreviousAsWatched = get(),
            resetProgress = get(),
            saveProgress = get(),
        )
    }

    viewModelOf(::CustomizationViewModel)

    viewModelOf(::HomeViewModel)

    viewModel { params ->
        SearchViewModel(
            contentType = params.getOrNull(),
            catalogUC = get(),
            settingsDataStore = get()
        )
    }

    viewModelOf(::SettingsViewModel)

    viewModel { params ->
        ShowViewModel(
            artworkId = params.get(),
            artworkUC = get(),
            resetProgress = get(),
        )
    }

    viewModelOf(::SourcesViewModel)

    viewModel { params ->
        TokenViewModel(
            fromSettings = params.get(),
            tokenDataStore = get(),
            tmdbService = get(),
            catalogUC = get(),
            appInfo = get()
        )
    }

    viewModelOf(::UnknownViewModel)

    viewModelOf(::WelcomeViewModel)

}