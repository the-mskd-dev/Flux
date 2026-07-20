package com.mskd.flux.core.network.tmdb.data

import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSourceImpl
import com.mskd.flux.core.network.tmdb.data.repository.ArtworkRemoteRepositoryImpl
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.data.service.TMDBServiceImpl
import com.mskd.flux.core.network.tmdb.domain.repository.ArtworkRemoteRepository
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val moduleTmdb = module {

    val baseUrl = "https://api.themoviedb.org/3/"

    single<Json> {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            isLenient = true
            useAlternativeNames = true
        }
    }

    single<HttpClient> {
        val json = get<Json>()
        val tokenDataStore = get<TokenDataStore>()

        HttpClient(OkHttp) {

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { _, response -> response.status.value == 429 }
                exponentialDelay(base = 1.0, maxDelayMs = 3000)
            }

            defaultRequest {
                url(baseUrl)
                headers.append(HttpHeaders.Accept, "application/json")
                val token = runBlocking { tokenDataStore.getToken() }
                if (token.isNotEmpty()) {
                    headers.append(HttpHeaders.Authorization, "Bearer ${token.trim()}")
                }
            }
        }

    }

    single<TMDBService> { TMDBServiceImpl(client = get()) }

    single<TmdbDataSource> {
        TmdbDataSourceImpl(
            tmdbService = get(),
            settings = get()
        )
    }

    single<ArtworkRemoteRepository> {
        ArtworkRemoteRepositoryImpl(
            tmdb = get()
        )
    }

}