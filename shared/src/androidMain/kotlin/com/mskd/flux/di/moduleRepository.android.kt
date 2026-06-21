package com.mskd.flux.di

import com.mskd.flux.data.repository.files.FilesRepositoryImpl
import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.files.FilesRepository
import org.koin.dsl.module

val moduleRepositoryAndroid = module {

    single<ConnectivityRepository> {
        `ConnectivityRepository.android`(context = get())
    }

    single<FilesRepository> {
        FilesRepositoryImpl(
            context = get(),
            userRepository = get()
        )
    }
}