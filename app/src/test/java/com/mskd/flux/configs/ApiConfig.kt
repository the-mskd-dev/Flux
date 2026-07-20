package com.mskd.flux.configs

import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.data.service.TMDBServiceImpl
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockWebServer

class ApiConfig : TestListener {

    lateinit var mockWebServer: MockWebServer
    lateinit var api: TMDBService

    override suspend fun beforeSpec(spec: Spec) {

        // Start server
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            coerceInputValues = true
            isLenient = true
            useAlternativeNames = true
        }

        // Create api
        val client = HttpClient(OkHttp) {
            engine {
                addInterceptor(object : okhttp3.Interceptor {
                    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
                        val response = chain.proceed(chain.request())
                        if (response.header("Content-Type") == null) {
                            return response.newBuilder()
                                .header("Content-Type", "application/json; charset=utf-8")
                                .build()
                        }
                        return response
                    }
                })
            }
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url(mockWebServer.url("/").toString())
            }
        }

        api = TMDBServiceImpl(client)
    }

    override suspend fun afterSpec(spec: Spec) {
        mockWebServer.close()
    }

}