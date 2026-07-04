package com.mskd.flux.di

import com.mskd.flux.data.useCases.artwork.ArtworkUC
import com.mskd.flux.data.useCases.artwork.ArtworkUCImpl
import com.mskd.flux.data.useCases.catalog.CatalogUC
import com.mskd.flux.data.useCases.catalog.CatalogUCImpl
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
            getFilesUseCase = get(),
            filterExistingFilesUseCase = get(),
            user = get(),
            settings = get(),
            imagesPrefetchManager = get(),
            metadataProvider = get(),
            appInfo = get(),
            scope = get(Qualifiers.APPLICATION_SCOPE),
        )
    }

}