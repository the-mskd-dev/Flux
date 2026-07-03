package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import com.mskd.flux.data.repository.sources.SourcesFilesRepository
import com.mskd.flux.features.files.data.MediaStoreFilesRepository
import com.mskd.flux.features.files.data.SafFilesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleRepositoryAndroid = module {

    single<ConnectivityRepository> {
        ConnectivityRepositoryImpl(context = get())
    }

    single<SourcesFilesRepository>(QualifiersAndroid.MEDIASTORE_SOURCES) {
        MediaStoreFilesRepository(
            context = get(),
            userRepository = get()
        )
    }

    singleOf(::SafVideoFilesDataSource)

    single<SourcesFilesRepository>(QualifiersAndroid.SAF_SOURCES) {
        SafFilesRepository(
            sourcesRepository = get(),
            safVideoFilesDataSource = get()
        )
    }

}