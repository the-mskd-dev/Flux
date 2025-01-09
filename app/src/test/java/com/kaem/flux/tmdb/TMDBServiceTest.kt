package com.kaem.flux.tmdb

import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.mockups.TMDBResponseMockups
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TMDBServiceTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: TMDBService

    @Before
    fun setUp() {

        // Start server
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Create api
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/")) // Utiliser l'URL du MockWebServer
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient())
            .build()

        api = retrofit.create(TMDBService::class.java)

    }

    @Test
    fun get_movies_success() = runBlocking {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movies)
            .setResponseCode(200)
        mockWebServer.enqueue(mockResponse)

        val tmdbResult = api.getMovie("your name")

        assert(tmdbResult.results.isNotEmpty())
        assert(tmdbResult.resultCount > 1)
        assert(tmdbResult.pageCount > 1)

    }

    @Test
    fun get_movies_fail() = runBlocking {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        try {
            api.getMovie("your name")
            assert(false) { "we should have an exception" }
        } catch (e: Exception) {
            assert(true)
        }

    }

    @After
    fun close() {
        mockWebServer.close()
    }

}