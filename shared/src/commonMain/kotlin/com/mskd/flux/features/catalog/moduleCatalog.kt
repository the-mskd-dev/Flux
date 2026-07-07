package com.mskd.flux.features.catalog

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCaseImpl
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCaseImpl
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCaseImpl
import org.koin.dsl.module

val moduleCatalog = module {

    single<CatalogSyncCoordinator> {
        CatalogSyncCoordinator(scope = get(Qualifiers.APPLICATION_SCOPE))
    }

    single<CleanCatalogUseCase> {
        CleanCatalogUseCaseImpl(
            getDeviceFilesUseCase = get(),
            database = get()
        )
    }

    single<SyncCatalogUseCase> {
        SyncCatalogUseCaseImpl(
            tmdb = get(),
            database = get(),
            user = get(),
            settings = get(),
            imagesPrefetchManager = get(),
            appInfo = get(),
            filterExistingFilesUseCase = get(),
            getDeviceFilesUseCase = get(),
            getFileDurationUseCase = get(),
            coordinator = get(),
        )
    }

    single<UpdateLanguageUseCase> {
        UpdateLanguageUseCaseImpl(
            tmdb = get(),
            database = get(),
            settings = get(),
            coordinator = get()
        )
    }

}