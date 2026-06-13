package com.mskd.flux.di

import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.data.tmdb.TMDBServiceImpl
import com.mskd.flux.data.tmdb.token.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val ktorModule = module {

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
        val tokenRepository = get<TokenRepository>()

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
                val token = runBlocking { tokenRepository.getToken() }
                if (token.isNotEmpty()) {
                    headers.append(HttpHeaders.Authorization, "Bearer ${token.trim()}")
                }
            }
        }

    }

    singleOf(::TMDBServiceImpl) bind TMDBService::class

}