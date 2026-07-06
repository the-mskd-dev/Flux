package com.mskd.flux.features.images

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.images.domain.ImagesPrefetchManagerImpl
import org.koin.dsl.module

val moduleImages = module {

    single<ImagesPrefetchManager> {
        ImagesPrefetchManagerImpl(
            database = get(),
            settings = get(),
            imageLoader = get(),
            imageRequestFactory = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE)
        )
    }

}