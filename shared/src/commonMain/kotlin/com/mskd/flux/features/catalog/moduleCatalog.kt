package com.mskd.flux.features.catalog

import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinatorImpl
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkFolderFetcher
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkFolderFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcherImpl
import com.mskd.flux.features.catalog.domain.resolver.EpisodeResolver
import com.mskd.flux.features.catalog.domain.resolver.EpisodeResolverImpl
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCaseImpl
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCaseImpl
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCaseImpl
import com.mskd.flux.features.catalog.presentation.CatalogViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val moduleCatalog = module {

    single<CoroutineDispatcher>(named("catalogSyncDispatcher")) {
        Dispatchers.IO.limitedParallelism(10)
    }

    single<ArtworkFolderFetcher> {
        ArtworkFolderFetcherImpl(
            remoteRepository = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<MovieMetadataFetcher> {
        MovieMetadataFetcherImpl(
            remoteRepository = get(),
            getFileDurationUseCase = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<SeasonMetadataFetcher>{
        SeasonMetadataFetcherImpl(
            remoteRepository = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
        )
    }

    single<EpisodeResolver> {
        EpisodeResolverImpl(
            remoteRepository = get(),
            settings = get(),
            getFileDurationUseCase = get(),
            dispatcher = get(named("catalogSyncDispatcher"))
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
            database = get(),
            user = get(),
            imagesPrefetchManager = get(),
            appInfo = get(),
            coordinator = get(),
            getDeviceFilesUseCase = get(),
            filterExistingFilesUseCase = get(),
            artworkFolderFetcher = get(),
            movieMetadataFetcher = get(),
            seasonMetadataFetcher = get(),
            episodeResolver = get()
        )
    }

    single<UpdateLanguageUseCase> {
        UpdateLanguageUseCaseImpl(
            remoteRepository = get(),
            database = get(),
            settings = get(),
            coordinator = get()
        )
    }

    viewModelOf(::CatalogViewModel)

}