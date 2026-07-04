package com.mskd.flux.core.util

import com.mskd.flux.core.util.images.ImagesPrefetchManager
import com.mskd.flux.core.util.images.ImagesPrefetchManagerImpl
import com.mskd.flux.di.Qualifiers
import org.koin.dsl.module

val moduleUtil = module {

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