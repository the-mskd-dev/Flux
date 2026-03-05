package com.kaem.flux.configs

import com.kaem.flux.data.tmdb.TMDBService
import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig : TestListener {

    lateinit var mockWebServer: MockWebServer
    lateinit var api: TMDBService

    override suspend fun beforeSpec(spec: Spec) {

        // Start server
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create api
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // URL of MockWebServer
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()

        api = retrofit.create(TMDBService::class.java)
    }

    override suspend fun afterSpec(spec: Spec) {
        mockWebServer.close()
    }

}