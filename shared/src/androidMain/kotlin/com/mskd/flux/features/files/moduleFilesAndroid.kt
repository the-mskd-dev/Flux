package com.mskd.flux.features.files

import com.mskd.flux.features.files.data.MediaStoreFilesRepository
import com.mskd.flux.features.files.data.SafFilesRepository
import com.mskd.flux.features.files.domain.repository.FilesRepository
import com.mskd.flux.features.files.domain.useCase.AndroidGetFilesUseCase
import com.mskd.flux.features.files.domain.useCase.GetFilesUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val MEDIASTORE_SOURCES = named("MEDIASTORE_SOURCES")
val SAF_SOURCES = named("SAF_SOURCES")

val moduleFilesAndroid = module {

    single<FilesRepository>(MEDIASTORE_SOURCES) {
        MediaStoreFilesRepository(
            context = get(),
            userRepository = get()
        )
    }

    single<FilesRepository>(SAF_SOURCES) {
        SafFilesRepository(
            context = get(),
            dataSource = get(),
            folderValidator = get()
        )
    }

    single<GetFilesUseCase> {
        AndroidGetFilesUseCase(
            mediaStore = get(MEDIASTORE_SOURCES),
            saf = get(SAF_SOURCES)
        )
    }

}