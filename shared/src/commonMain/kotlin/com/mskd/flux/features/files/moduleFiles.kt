package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.usecase.ScanSourcesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.ScanSourcesUseCase
import org.koin.dsl.module

val moduleFiles = module {

    single<GetDeviceFilesUseCase> {
        GetDeviceFilesUseCaseImpl(
            sourcesProvider = get()
        )
    }

    single<GetSubtitlesUseCase> {
        GetSubtitlesUseCaseImpl(
            sourcesProvider = get()
        )
    }

    single<ScanSourcesUseCase> {
        ScanSourcesUseCaseImpl(
            sourcesProvider = get()
        )
    }

}
