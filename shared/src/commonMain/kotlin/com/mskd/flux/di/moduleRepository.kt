package com.mskd.flux.di

import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.repository.SourcesRepositoryImpl
import com.mskd.flux.features.tmdb.data.dataSource.TmdbDataSource
import com.mskd.flux.features.tmdb.data.dataSource.TmdbDataSourceImpl
import org.koin.dsl.module


val moduleRepository = module {



    single<SourcesRepository> {
        SourcesRepositoryImpl(
            dao = get(),
            userFolderValidator = get()
        )
    }
}