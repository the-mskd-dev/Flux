package com.mskd.flux.features.sources

import com.mskd.flux.data.local.ddb.FluxDatabase
import com.mskd.flux.data.local.ddb.SourcesDao
import com.mskd.flux.features.sources.data.dataSource.SourcesDataSource
import com.mskd.flux.features.sources.data.dataSource.SourcesDataSourceImpl
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.repository.SourcesRepositoryImpl
import com.mskd.flux.features.sources.domain.useCase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.useCase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.useCase.FlowSourcesUseCase
import com.mskd.flux.features.sources.domain.useCase.GetSourcesUseCase
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