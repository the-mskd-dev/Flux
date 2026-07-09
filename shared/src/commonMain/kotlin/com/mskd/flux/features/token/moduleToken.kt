package com.mskd.flux.features.token

import com.mskd.flux.features.token.data.usecase.SaveTokenAndSyncUseCaseImpl
import com.mskd.flux.features.token.domain.usecase.SaveTokenAndSyncUseCase
import com.mskd.flux.features.token.presentation.TokenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleToken = module {

    single<SaveTokenAndSyncUseCase> {
        SaveTokenAndSyncUseCaseImpl(
            tokenDataStore = get(),
            tmdbService = get(),
            syncCatalogUseCase = get()
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

}