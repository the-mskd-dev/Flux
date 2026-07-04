package com.mskd.flux.core.util

import com.mskd.flux.core.util.connectivity.AndroidConnectivityRepository
import com.mskd.flux.core.util.connectivity.ConnectivityRepository
import com.mskd.flux.core.util.images.AndroidImageRequestFactory
import com.mskd.flux.features.images.data.ImageRequestFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleUtilAndroid = module {

    single<ConnectivityRepository> {
        AndroidConnectivityRepository(context = get())
    }

    single<ImageRequestFactory> {
        AndroidImageRequestFactory(context = androidContext())
    }


}