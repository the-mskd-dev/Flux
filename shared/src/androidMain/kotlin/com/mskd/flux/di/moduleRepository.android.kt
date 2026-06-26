package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.files.FilesRepositoryImpl
import org.koin.dsl.module

val moduleRepositoryAndroid = module {

    single<ConnectivityRepository> {
        ConnectivityRepositoryImpl(context = get())
    }

    single<FilesRepository> {
        FilesRepositoryImpl(
            context = get(),
            userRepository = get()
        )
    }
}