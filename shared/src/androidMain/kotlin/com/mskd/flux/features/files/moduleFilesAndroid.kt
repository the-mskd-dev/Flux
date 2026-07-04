package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.AndroidMetadataProvider
import com.mskd.flux.features.files.data.MediaStoreFilesRepository
import com.mskd.flux.features.files.data.SafFilesRepository
import com.mskd.flux.features.files.data.usecase.AndroidFilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.files.data.usecase.AndroidGetFilesUseCase
import com.mskd.flux.features.files.data.usecase.AndroidGetSubtitlesUseCase
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetSubtitlesUseCase
import com.mskd.flux.platform.MetadataProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val MEDIASTORE_SOURCES = named("MEDIASTORE_SOURCES")
val SAF_SOURCES = named("SAF_SOURCES")

val moduleFilesAndroid = module {

    single<FilesRepository>(MEDIASTORE_SOURCES) {
        MediaStoreFilesRepository(
            context = get(),
            userDataStore = get()
        )
    }

    single<FilesRepository>(SAF_SOURCES) {
        SafFilesRepository(
            context = get(),
            dataSource = get(),
            folderValidator = get()
        )
    }

    single<MetadataProvider> {
        AndroidMetadataProvider(context = androidContext())
    }

    single<GetFilesUseCase> {
        AndroidGetFilesUseCase(
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