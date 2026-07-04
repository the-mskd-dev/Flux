package com.mskd.flux.features.sources

import com.mskd.flux.core.data.database.FluxDatabase
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.datasource.SourcesDataSource
import com.mskd.flux.features.sources.data.datasource.SourcesDataSourceImpl
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.repository.SourcesRepositoryImpl
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.features.sources.domain.usecase.GetSourcesUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val moduleSources = module {

    single<SourcesDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.sourcesDao()
    }

    single<SourcesDataSource> {
        SourcesDataSourceImpl(dao = get())
    }

    single<SourcesRepository> {
        SourcesRepositoryImpl(
            dataSource = get(),
            userFolderValidator = get()
        )
    }

    singleOf(::GetSourcesUseCase)
    singleOf(::FlowSourcesUseCase)
    singleOf(::AddSourceUseCase)
    singleOf(::DeleteSourceUseCase)

}