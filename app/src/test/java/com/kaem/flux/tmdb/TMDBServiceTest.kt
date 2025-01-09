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
            .baseUrl(mockWebServer.url("/")) // URL of MockWebServer
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
        assert(tmdbResult.resultCount > 0)
        assert(tmdbResult.pageCount > 0)

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

    @Test
    fun get_shows_success() = runBlocking {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.shows)
            .setResponseCode(200)
        mockWebServer.enqueue(mockResponse)

        val tmdbResult = api.getShow("naruto")

        assert(tmdbResult.results.isNotEmpty())
        assert(tmdbResult.resultCount > 0)
        assert(tmdbResult.pageCount > 0)

    }

    @Test
    fun get_shows_fail() = runBlocking {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        mockWebServer.enqueue(mockResponse)

        try {
            api.getShow("naruto")
            assert(false) { "we should have an exception" }
        } catch (e: Exception) {
            assert(true)
        }

    }

    @Test
    fun get_movie_details() = runBlocking {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movie)
            .setResponseCode(200)
        mockWebServer.enqueue(mockResponse)

        val tmdbResult = api.getMovieDetails(0L)

        assert(tmdbResult.title.isNotEmpty())

    }

    @Test
    fun get_episode_details() = runBlocking {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.episode)
            .setResponseCode(200)
        mockWebServer.enqueue(mockResponse)

        val tmdbResult = api.getEpisode(0L, 1, 1)

        assert(tmdbResult.title.isNotEmpty())

    }

    @After
    fun close() {
        mockWebServer.close()
    }

}