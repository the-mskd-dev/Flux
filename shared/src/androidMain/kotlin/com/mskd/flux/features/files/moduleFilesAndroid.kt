package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.AndroidMetadataProvider
import com.mskd.flux.features.files.data.datasource.MediaStoreFilesDataSource
import com.mskd.flux.features.files.data.datasource.SafFilesDataSource
import com.mskd.flux.features.files.data.usecase.AndroidFilterExistingFilesUseCase
import com.mskd.flux.features.files.data.usecase.AndroidGetDeviceFilesUseCase
import com.mskd.flux.features.files.data.usecase.AndroidGetSubtitlesUseCase
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.platform.MetadataProvider
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

    single<MetadataProvider> {
        AndroidMetadataProvider(context = androidContext())
    }

    single<GetDeviceFilesUseCase> {
        AndroidGetDeviceFilesUseCase(
            mediaStore = get(MEDIASTORE_SOURCES),
            saf = get(SAF_SOURCES)
        )
    }

    single<FilterExistingFilesUseCase> {
        AndroidFilterExistingFilesUseCase(
            mediaStore = get(MEDIASTORE_SOURCES),
            saf = get(SAF_SOURCES)
        )
    }

    single<GetSubtitlesUseCase> {
        AndroidGetSubtitlesUseCase(
            mediaStore = get(MEDIASTORE_SOURCES),
            saf = get(SAF_SOURCES)
        )
    }

}