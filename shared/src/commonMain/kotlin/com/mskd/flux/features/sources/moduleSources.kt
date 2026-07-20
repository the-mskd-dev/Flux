package com.mskd.flux.features.sources

import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.repository.SourcesRepositoryImpl
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.usecase.AddSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteSourceUseCase
import com.mskd.flux.features.sources.domain.usecase.DeleteUnavailableSourcesUseCase
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
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
            databaseRepository = get(),
            userFolderValidator = get()
        )
    }

    singleOf(::DeleteUnavailableSourcesUseCase)
    singleOf(::FlowSourcesUseCase)
    singleOf(::AddSourceUseCase)
    singleOf(::DeleteSourceUseCase)

    viewModel { params ->
        SourcesViewModel(
            fromSetup = params.get(),
            userDataStore = get(),
            flowSourcesUseCase = get(),
            addSourceUseCase = get(),
            deleteSourceUseCase = get(),
            syncCatalogUseCase = get(),
        )
    }

}