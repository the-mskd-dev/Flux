package com.mskd.flux.features.catalog

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.catalog.data.datastore.CatalogDataStoreImpl
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinatorImpl
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.sync.SyncPipeline
import com.mskd.flux.features.catalog.domain.sync.enrichment.DetailsEnrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.DurationsEnrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.IdentificationEnrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.SeasonsEnrichment
import com.mskd.flux.features.catalog.domain.sync.step.CatalogStep
import com.mskd.flux.features.catalog.domain.sync.step.FinalizationStep
import com.mskd.flux.features.catalog.domain.sync.step.GenresStep
import com.mskd.flux.features.catalog.domain.sync.step.LogosStep
import com.mskd.flux.features.catalog.domain.sync.step.MissingFilesStep
import com.mskd.flux.features.catalog.domain.sync.step.NsfwPrivateStep
import com.mskd.flux.features.catalog.domain.sync.step.ScanStep
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.catalog.presentation.CatalogViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleCatalog = module {

    singleOf(::ScanStep)
    singleOf(::MissingFilesStep)
    singleOf(::GenresStep)
    singleOf(::NsfwPrivateStep)
    singleOf(::LogosStep)
    singleOf(::FinalizationStep)

    single {
        CatalogStep(
            database = get(),
            enrichments = listOf(
                IdentificationEnrichment(api = get()),
                DetailsEnrichment(api = get()),
                SeasonsEnrichment(api = get()),
                DurationsEnrichment(getFileDurationUseCase = get()),
            ),
        )
    }

    single {
        SyncPipeline(
            scanStep = get(),
            steps = listOf(
                get<MissingFilesStep>(),
                get<GenresStep>(),
                get<CatalogStep>(),
                get<LogosStep>(),
                get<NsfwPrivateStep>(),
                get<FinalizationStep>(),
            )
        )
    }

    single<CatalogSyncCoordinator> {
        CatalogSyncCoordinatorImpl(scope = get(Qualifiers.APPLICATION_SCOPE))
    }

    singleOf(::SyncCatalogUseCase)
    singleOf(::SyncGenresUseCase)

    single<UpdateLanguageUseCase> {
        UpdateLanguageUseCase(
            api = get(),
            database = get(),
            settings = get(),
            coordinator = get(),
            syncGenresUseCase = get(),
        )
    }

    single<CatalogDataStore> {
        CatalogDataStoreImpl(
            catalogDataStore = get(Qualifiers.CATALOG_DATASTORE),
        )
    }

    viewModelOf(::CatalogViewModel)

}