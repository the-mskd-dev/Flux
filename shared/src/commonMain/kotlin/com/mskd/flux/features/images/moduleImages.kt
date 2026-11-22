package com.mskd.flux.features.images

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.images.data.usecase.PrefetchImagesUseCaseImpl
import com.mskd.flux.features.images.domain.usecase.PrefetchImagesUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

val moduleImages = module {

    single<PrefetchImagesUseCase> {
        PrefetchImagesUseCaseImpl(
            database = get(),
            imageLoader = get(named("cacheImageLoader")),
            imageRequestFactory = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE)
        )
    }

}