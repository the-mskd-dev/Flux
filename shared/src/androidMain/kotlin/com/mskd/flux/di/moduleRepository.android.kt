package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import org.koin.dsl.module

val moduleRepositoryAndroid = module {

    single<ConnectivityRepository> {
        ConnectivityRepositoryImpl(context = get())
    }

}