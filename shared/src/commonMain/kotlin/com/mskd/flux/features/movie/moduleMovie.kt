package com.mskd.flux.features.movie

import com.mskd.flux.features.movie.presentation.MovieViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleMovie = module {

    viewModel { params ->
        MovieViewModel(
            artworkId = params.get<Long>(),
            filesLauncher = get(),
            urlLauncher = get(),
            tokenDataStore = get(),
            catalogSyncCoordinator = get(),
            observeArtworkUseCase = get(),
            getPeopleUseCase = get(),
            progressTracker = get(),
            playbackLaunch = get(),
        )
    }

}
