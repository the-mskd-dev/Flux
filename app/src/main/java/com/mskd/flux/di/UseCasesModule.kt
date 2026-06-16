package com.mskd.flux.di

import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.artwork.ArtworkUCImpl
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.catalog.CatalogUCImpl
import com.mskd.flux.useCases.images.ImagesUC
import com.mskd.flux.useCases.images.ImagesUCImpl
import com.mskd.flux.useCases.player.PipIsEnabledUC
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.useCases.progress.ProgressUCImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val useCasesModule = module {

    single<CatalogUC> {
        CatalogUCImpl(
            tmdb = get(),
            database = get(),
            files = get(),
            user = get(),
            settings = get(),
            imagesUC = get(),
            scope = get(named("ApplicationScope")),
            context = androidContext()
        )
    }

    singleOf(::ArtworkUCImpl) bind ArtworkUC::class

    singleOf(::ProgressUCImpl) bind ProgressUC::class

    single<ImagesUC> {
        ImagesUCImpl(
            database = get(),
            imageLoader = get(),
            settings = get(),
            context = androidContext(),
            scope = get(named("ApplicationScope"))
        )
    }

    singleOf(::PipIsEnabledUC)

}