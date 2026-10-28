package com.mskd.flux.core.network.tmdb.data

import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSourceImpl
import com.mskd.flux.core.network.tmdb.data.repository.ApiRepositoryImpl
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.data.service.TMDBServiceImpl
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val moduleTmdb = module {

    single<Json> { tmdbJson() }

    single<HttpClient> {
        tmdbHttpClient(
            engine = tmdbEngine(),
            json = get(),
            tokenDataStore = get()
        )
    }

    single<TMDBService> { TMDBServiceImpl(client = get()) }

    single<TmdbDataSource> {
        TmdbDataSourceImpl(
            tmdbService = get(),
            settings = get(),
            tokenDataStore = get()
        )
    }

    single<ApiRepository> {
        ApiRepositoryImpl(
            tmdb = get()
        )
    }

}