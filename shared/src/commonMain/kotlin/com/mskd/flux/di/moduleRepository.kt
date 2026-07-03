package com.mskd.flux.di

import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.repository.SourcesRepositoryImpl
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import org.koin.dsl.module


val moduleRepository = module {

    single<TmdbRepository> {
        TmdbRepositoryImpl(
            tmdbService = get(),
            settings = get()
        )
    }

    single<SourcesRepository> {
        SourcesRepositoryImpl(
            dao = get(),
            userFolderValidator = get()
        )
    }
}