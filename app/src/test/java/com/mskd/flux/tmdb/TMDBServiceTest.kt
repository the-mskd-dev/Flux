package com.mskd.flux.tmdb

import com.mskd.flux.configs.ApiConfig
import com.mskd.flux.mockups.TMDBResponseMockups
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse

class TMDBServiceTest : FunSpec({

    val apiConfig = ApiConfig()
    extension(apiConfig)

    test("get movies success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movies)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.getMovie("your name")

        tmdbResult.results.isNotEmpty() shouldBe true
        tmdbResult.resultCount shouldBeGreaterThan 0
        tmdbResult.pageCount shouldBeGreaterThan 0

    }

    test("get movies fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getMovie("your name")
        }

    }

    test("get shows success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.shows)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.getShow("naruto")

        tmdbResult.results.isNotEmpty() shouldBe true
        tmdbResult.resultCount shouldBeGreaterThan 0
        tmdbResult.pageCount shouldBeGreaterThan 0

    }

    test("get shows fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getShow("naruto")
        }

    }

    test("get movie details") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movie)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.getMovieDetails(0L)

        tmdbResult.title.isNotEmpty() shouldBe true

    }

    test("get episode details") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.episode)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.getEpisode(0L, 1, 1)

        tmdbResult.title.isNotEmpty() shouldBe true

    }

})