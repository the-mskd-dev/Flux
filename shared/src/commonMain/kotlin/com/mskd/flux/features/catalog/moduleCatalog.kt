package com.mskd.flux.features.catalog

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.catalog.data.datastore.CatalogDataStoreImpl
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinatorImpl
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.CatalogContentFetcher
import com.mskd.flux.features.catalog.domain.fetcher.CatalogContentFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.resolver.MediaResolver
import com.mskd.flux.features.catalog.domain.resolver.MediaResolverImpl
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.migration.LegacyGenresMigration
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.catalog.presentation.CatalogViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val moduleCatalog = module {

    single<CoroutineDispatcher>(named("catalogSyncDispatcher")) {
        Dispatchers.IO.limitedParallelism(10)
    }

    single<ArtworkMetadataFetcher> {
        ArtworkMetadataFetcherImpl(
            api = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<MovieMetadataFetcher> {
        MovieMetadataFetcherImpl(
            api = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<SeasonMetadataFetcher>{
        SeasonMetadataFetcherImpl(
            api = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<MediaResolver> {
        MediaResolverImpl(
            api = get(),
            settings = get(),
            getFileDurationUseCase = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<CatalogContentFetcher> {
        CatalogContentFetcherImpl(
            artworkMetadataFetcher = get(),
            movieMetadataFetcher = get(),
            seasonMetadataFetcher = get(),
            mediaResolver = get()
        )
    }

    single<CatalogSyncCoordinator> {
        CatalogSyncCoordinatorImpl(scope = get(Qualifiers.APPLICATION_SCOPE))
    }

    singleOf(::CleanCatalogUseCase)
    singleOf(::SyncCatalogUseCase)
    singleOf(::SyncGenresUseCase)
    singleOf(::LegacyGenresMigration)

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