package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.datasource.MediaStoreFilesDataSource
import com.mskd.flux.features.files.data.datasource.SafFilesDataSource
import com.mskd.flux.features.files.data.usecase.AndroidGetFileDurationUseCase
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.ScanSourcesUseCase
import com.mskd.flux.features.files.data.usecase.ScanSourcesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCaseImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val MEDIASTORE_SOURCES = named("MEDIASTORE_SOURCES")
val SAF_SOURCES = named("SAF_SOURCES")

val moduleFilesAndroid = module {

    single<FilesDataSource>(MEDIASTORE_SOURCES) {
        MediaStoreFilesDataSource(
            context = get(),
            userDataStore = get()
        )
    }

    factory<FilesDataSource>(SAF_SOURCES) { params ->
        SafFilesDataSource(
            context = get(),
            folder = params.get()
        )
    }

    single<GetFileDurationUseCase> {
        AndroidGetFileDurationUseCase(context = androidContext())
    }

    single<GetDeviceFilesUseCase> {
        GetDeviceFilesUseCaseImpl(
            sourcesProvider = get()
        )
    }

    single<ScanSourcesUseCase> {
        ScanSourcesUseCaseImpl(
            sourcesProvider = get()
        )
    }

    single<GetSubtitlesUseCase> {
        GetSubtitlesUseCaseImpl(
            sourcesProvider = get()
        )
    }

}