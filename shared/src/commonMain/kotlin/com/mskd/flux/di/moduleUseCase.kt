package com.mskd.flux.di

import com.mskd.flux.data.useCases.artwork.ArtworkUC
import com.mskd.flux.data.useCases.artwork.ArtworkUCImpl
import org.koin.dsl.module

val moduleUseCase = module {

    single<ArtworkUC> {
        ArtworkUCImpl(
            database = get()
        )
    }

}