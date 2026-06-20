package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import com.mskd.flux.shared.data.repository.connectivity.ConnectivityRepository
import org.koin.dsl.module

val moduleRepositoryAndroid = module {

    single<ConnectivityRepository> {
        ConnectivityRepositoryImpl(context = get())
    }

}