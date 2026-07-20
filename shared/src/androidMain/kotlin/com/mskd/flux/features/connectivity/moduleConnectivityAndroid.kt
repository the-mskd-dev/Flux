package com.mskd.flux.features.connectivity

import com.mskd.flux.features.connectivity.data.AndroidConnectivityRepository
import com.mskd.flux.features.connectivity.domain.ConnectivityRepository
import org.koin.dsl.module

val moduleConnectivityAndroid = module {

    single<ConnectivityRepository> {
        AndroidConnectivityRepository(context = get())
    }


}