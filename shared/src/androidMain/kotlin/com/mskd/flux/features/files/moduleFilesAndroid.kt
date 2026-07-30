package com.mskd.flux.features.files

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.features.files.data.datasource.MediaStoreFilesDataSource
import com.mskd.flux.features.files.data.datasource.SafFilesDataSource
import com.mskd.flux.features.files.data.usecase.AndroidGetFileDurationUseCase
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCaseImpl
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

    single<FilesDataSource>(SAF_SOURCES) {
        SafFilesDataSource(
            context = get(),
            sources = get()
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

    single<FilterExistingFilesUseCase> {
        FilterExistingFilesUseCaseImpl(
            sourcesProvider = get()
        )
    }

    single<GetSubtitlesUseCase> {
        GetSubtitlesUseCaseImpl(
            sources = mapOf(
                FileSource.LOCAL to get(MEDIASTORE_SOURCES),
                FileSource.SAF to get(SAF_SOURCES),
            ),
            sourcesProvider = get()
        )
    }

}