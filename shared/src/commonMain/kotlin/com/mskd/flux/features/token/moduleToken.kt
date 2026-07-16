package com.mskd.flux.features.token

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.token.data.datastore.TokenDataStoreImpl
import com.mskd.flux.features.token.data.usecase.SaveTokenAndSyncUseCaseImpl
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.features.token.domain.usecase.SaveTokenAndSyncUseCase
import com.mskd.flux.features.token.presentation.TokenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleToken = module {

    single<TokenDataStore> {
        TokenDataStoreImpl(
            tokenDataStore = get(Qualifiers.TOKEN_DATASTORE)
        )
    }

    single<SaveTokenAndSyncUseCase> {
        SaveTokenAndSyncUseCaseImpl(
            tokenDataStore = get(),
            tmdbService = get(),
            syncCatalogUseCase = get()
        )
    }

    viewModel { params ->
        TokenViewModel(
            fromSetup = params.get(),
            tokenDataStore = get(),
            saveTokenAndSyncUseCase = get(),
            appInfo = get()
        )
    }

}