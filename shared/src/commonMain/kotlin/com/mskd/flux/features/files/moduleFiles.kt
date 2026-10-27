package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.usecase.ScanSourcesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.ScanSourcesUseCase
import org.koin.dsl.module

val moduleFiles = module {

    single<ScanSourcesUseCase> {
        ScanSourcesUseCaseImpl(
            sourcesProvider = get()
        )
    }

}
