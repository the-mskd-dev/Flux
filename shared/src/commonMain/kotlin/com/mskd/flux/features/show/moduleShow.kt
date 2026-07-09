package com.mskd.flux.features.show

import com.mskd.flux.features.show.presentation.ShowViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleShow = module {

    viewModel { params ->
        ShowViewModel(
            artworkId = params.get(),
            observeArtworkUseCase = get(),
            resetProgress = get(),
        )
    }

}