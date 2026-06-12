package com.mskd.flux.di

import com.mskd.flux.data.tmdb.token.TokenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.readJson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KtorModule {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        isLenient = true
        useAlternativeNames = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        tokenRepository: TokenRepository
    ): HttpClient = HttpClient(OkHttp) {

        // Sérialisation
        install(ContentNegotiation) {
            json(json)
        }

        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, response -> response.status.value == 429 }
            exponentialDelay(base = 1.0, maxDelayMs = 3000)
        }

        // Base URL + headers (équivalent TokenInterceptor)
        defaultRequest {
            url(BASE_URL)
            headers.append(HttpHeaders.Accept, "application/json")
            val token = runBlocking { tokenRepository.getToken() }
            if (token.isNotEmpty()) {
                headers.append(HttpHeaders.Authorization, "Bearer ${token.trim()}")
            }
        }
    }

}