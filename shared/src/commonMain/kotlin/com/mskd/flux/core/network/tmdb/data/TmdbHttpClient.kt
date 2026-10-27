package com.mskd.flux.core.network.tmdb.data

import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://api.themoviedb.org/3/"

/** Most TMDB requests in flight at the same time, whatever their origin (Synchro, language change, genres). */
const val TMDB_MAX_REQUESTS = 8

fun tmdbJson(): Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    isLenient = true
    useAlternativeNames = true
}

fun tmdbHttpClient(
    engine: HttpClientEngine,
    json: Json,
    tokenDataStore: TokenDataStore,
    maxRequests: Int = TMDB_MAX_REQUESTS,
): HttpClient = HttpClient(engine) {

    // Installed before the retry so that each attempt takes a place, and the wait between two attempts does not
    install(RequestLimiter) { limit = maxRequests }

    install(ContentNegotiation) {
        json(json)
    }

    install(HttpRequestRetry) {
        maxRetries = 3
        retryIf { _, response -> response.status.value == 429 }
        exponentialDelay(base = 1.0, maxDelayMs = 3000)
    }

    defaultRequest {
        url(BASE_URL)
        headers.append(HttpHeaders.Accept, "application/json")
        val token = runBlocking { tokenDataStore.getToken() }
        if (token.isNotEmpty()) {
            headers.append(HttpHeaders.Authorization, "Bearer ${token.trim()}")
        }
    }

}
