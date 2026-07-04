package com.mskd.flux.di

import com.mskd.flux.data.useCases.artwork.ArtworkUC
import com.mskd.flux.data.useCases.artwork.ArtworkUCImpl
import com.mskd.flux.data.useCases.catalog.CatalogUC
import com.mskd.flux.data.useCases.catalog.CatalogUCImpl
import com.mskd.flux.core.util.images.ImagesPrefetchManager
import com.mskd.flux.core.util.images.ImagesPrefetchManagerImpl
import com.mskd.flux.data.useCases.progress.ProgressUC
import com.mskd.flux.data.useCases.progress.ProgressUCImpl
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
            imagesPrefetchManager = get(),
            metadataProvider = get(),
            appInfo = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE),
        )
    }

}