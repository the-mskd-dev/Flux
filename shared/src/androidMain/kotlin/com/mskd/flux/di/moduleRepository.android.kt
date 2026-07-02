package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import com.mskd.flux.data.repository.files.mediaStore.MediaStoreRepository
import com.mskd.flux.data.repository.files.mediaStore.MediaStoreRepositoryImpl
import org.koin.dsl.module

val moduleRepositoryAndroid = module {

    single<ConnectivityRepository> {
        ConnectivityRepositoryImpl(context = get())
    }

    single<MediaStoreRepository> {
        MediaStoreRepositoryImpl(
            context = get(),
            userRepository = get()
        )
    }

}