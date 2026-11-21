package com.mskd.flux.features.season

import com.mskd.flux.features.season.presentation.SeasonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleSeason = module {

    viewModel { params ->
        SeasonViewModel(
            artworkId = params.get<Long>(),
            season = params.get<Int>(),
            filesLauncher = get(),
            urlLauncher = get(),
            settingsDataStore = get(),
            tokenDataStore = get(),
            catalogSyncCoordinator = get(),
            observeArtworkUseCase = get(),
            changeMediaStatus = get(),
            markPreviousAsWatched = get(),
            resetProgress = get(),
            resolvePlaybackAction = get(),
            recordPlaybackResult = get(),
        )
    }

}
