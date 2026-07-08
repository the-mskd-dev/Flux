package com.mskd.flux.features.token

import com.mskd.flux.features.token.data.usecase.SaveTokenAndSyncUseCaseImpl
import com.mskd.flux.features.token.domain.usecase.SaveTokenAndSyncUseCase
import org.koin.dsl.module

val moduleToken = module {

    single<SaveTokenAndSyncUseCase> {
        SaveTokenAndSyncUseCaseImpl(
            tokenDataStore = get(),
            tmdbService = get(),
            syncCatalogUseCase = get()
        )
    }
}