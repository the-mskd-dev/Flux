package com.mskd.flux.features.images

import com.mskd.flux.features.images.data.AndroidImageRequestFactory
import com.mskd.flux.features.images.domain.ImageRequestFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleImagesAndroid = module {

    single<ImageRequestFactory> {
        AndroidImageRequestFactory(context = androidContext())
    }

}