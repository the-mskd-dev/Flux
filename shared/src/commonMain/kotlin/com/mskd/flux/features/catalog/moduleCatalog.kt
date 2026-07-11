package com.mskd.flux.features.catalog

import com.mskd.flux.features.catalog.domain.resolver.EpisodeMetadataResolver
import com.mskd.flux.features.catalog.domain.resolver.EpisodeMetadataResolverImpl
import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinatorImpl
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCaseImpl
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCaseImpl
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCaseImpl
import com.mskd.flux.features.catalog.presentation.catalog.CatalogViewModel
import com.mskd.flux.features.catalog.presentation.search.SearchViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleCatalog = module {

    single<ArtworkMetadataFetcher> {
        ArtworkMetadataFetcherImpl(
            tmdb = get(),
            dispatcher = Dispatchers.IO.limitedParallelism(10)
        )
    }

    single<MovieMetadataFetcher> {
        MovieMetadataFetcherImpl(
            tmdb = get(),
            getFileDurationUseCase = get(),
            dispatcher = Dispatchers.IO.limitedParallelism(10)
        )
    }

    single<SeasonMetadataFetcher>{
        SeasonMetadataFetcherImpl(
            tmdb = get(),
            dispatcher = Dispatchers.IO.limitedParallelism(10)
        )
    }

    single<EpisodeMetadataResolver> {
        EpisodeMetadataResolverImpl(
            tmdb = get(),
            settings = get(),
            getFileDurationUseCase = get(),
            dispatcher = Dispatchers.IO.limitedParallelism(10)
        )
    }

    single<CatalogSyncCoordinator> {
        CatalogSyncCoordinatorImpl(scope = get(Qualifiers.APPLICATION_SCOPE))
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
            deleteUnavailableSourcesUseCase = get(),
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

    viewModelOf(::CatalogViewModel)

    viewModel { params ->
        SearchViewModel(
            contentType = params.getOrNull(),
            database = get(),
            settingsDataStore = get()
        )
    }

}