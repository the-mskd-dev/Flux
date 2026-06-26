package com.mskd.flux.di

import com.mskd.flux.utils.interceptors.NetworkImageInterceptor
import org.koin.dsl.module

val moduleImages = module {

    single<NetworkImageInterceptor> {
        NetworkImageInterceptor(connectivityRepository = get())
    }

}