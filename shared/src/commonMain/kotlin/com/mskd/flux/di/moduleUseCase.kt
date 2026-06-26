package com.mskd.flux.di

import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.artwork.ArtworkUCImpl
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.catalog.CatalogUCImpl
import com.mskd.flux.useCases.images.ImagesUC
import com.mskd.flux.useCases.images.ImagesUCImpl
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.useCases.progress.ProgressUCImpl
import org.koin.dsl.module

val moduleUseCase = module {

    single<ArtworkUC> {
        ArtworkUCImpl(
            database = get()
        )
    }

    single<CatalogUC> {
        CatalogUCImpl(
            tmdb = get(),
            database = get(),
            files = get(),
            user = get(),
            settings = get(),
            imagesUC = get(),
            metadataProvider = get(),
            appInfo = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE),
        )
    }

    single<ImagesUC> {
        ImagesUCImpl(
            database = get(),
            settings = get(),
            imageLoader = get(),
            imageRequestFactory = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE)
        )
    }

    single<ProgressUC> {
        ProgressUCImpl(
            database = get(),
            user = get()
        )
    }

}