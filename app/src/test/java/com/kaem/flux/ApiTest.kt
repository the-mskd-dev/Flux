package com.kaem.flux

import com.kaem.flux.data.tmdb.TMDBService
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class ApiTest {

    protected lateinit var mockWebServer: MockWebServer
    protected lateinit var api: TMDBService

    @Before
    open fun setUp() {

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

    @After
    open fun close() {
        mockWebServer.close()
    }

}