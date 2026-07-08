package com.mskd.flux.features.sources

import com.mskd.flux.core.data.database.FluxDatabase
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.repository.SourcesRepositoryImpl
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.features.sources.domain.usecase.FindUnavailableSourceUseCase
import com.mskd.flux.features.sources.presentation.SourcesViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moduleSources = module {

    single<SourcesDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.sourcesDao()
    }

    single<SourcesRepository> {
        SourcesRepositoryImpl(
            dao = get(),
            userFolderValidator = get()
        )
    }

    singleOf(::FindUnavailableSourceUseCase)
    singleOf(::FlowSourcesUseCase)
    singleOf(::AddSourceUseCase)
    singleOf(::DeleteSourceUseCase)

    viewModel { params ->
        SourcesViewModel(
            fromSetup = params.get(),
            tokenDataStore = get(),
            userDataStore = get(),
            flowSourcesUseCase = get(),
            addSourceUseCase = get(),
            deleteSourceUseCase = get(),
            syncCatalogUseCase = get(),
        )
    }

}