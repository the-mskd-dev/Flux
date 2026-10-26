package com.mskd.flux.tmdb

import com.mskd.flux.configs.ApiConfig
import com.mskd.flux.core.model.language.Language
import com.mskd.flux.mockups.TMDBResponseMockups
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import okhttp3.mockwebserver.MockResponse
import java.util.concurrent.TimeUnit

class TMDBServiceTest : FunSpec({

    val apiConfig = ApiConfig()
    extension(apiConfig)

    test("authenticate success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.authentication)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val result = apiConfig.api.authenticate()

        result.success shouldBe true

    }

    test("authenticate fail") {

        val mockResponse = MockResponse()
            .setResponseCode(401)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.authenticate()
        }

    }

    test("get movies success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movies)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.searchMovie("your name", language = Language.ENGLISH.code)

        tmdbResult.results.isNotEmpty() shouldBe true
        tmdbResult.resultCount shouldBeGreaterThan 0
        tmdbResult.pageCount shouldBeGreaterThan 0

    }

    test("get movies fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.searchMovie("your name", language = Language.ENGLISH.code)
        }

    }

    test("get movie details success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movie)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.getMovieDetails(0L, language = Language.ENGLISH.code)

        tmdbResult.title.isNotEmpty() shouldBe true

    }

    test("get movie details fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getMovieDetails(0L, language = Language.ENGLISH.code)
        }

    }

    test("get movie translations success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.translations)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val result = apiConfig.api.getMovieTranslations(372058L)

        result.translations.isNotEmpty() shouldBe true
        result.translations.any { it.language == Language.ENGLISH.code } shouldBe true

    }

    test("get movie translations fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getMovieTranslations(0L)
        }

    }

    test("get shows success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.shows)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.searchShow("naruto", language = Language.ENGLISH.code)

        tmdbResult.results.isNotEmpty() shouldBe true
        tmdbResult.resultCount shouldBeGreaterThan 0
        tmdbResult.pageCount shouldBeGreaterThan 0

    }

    test("get shows fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.searchShow("naruto", language = Language.ENGLISH.code)
        }

    }

    test("get show translations success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.translations)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val result = apiConfig.api.getShowTranslations(31910L)

        result.translations.isNotEmpty() shouldBe true
        result.translations.any { it.language == Language.ENGLISH.code } shouldBe true

    }

    test("get show translations fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getShowTranslations(0L)
        }

    }

    test("get episode details success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.episode)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val tmdbResult = apiConfig.api.getEpisode(0L, 1, 1, language = Language.ENGLISH.code)

        tmdbResult.title.isNotEmpty() shouldBe true

    }

    test("get episode details fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getEpisode(0L, 1, 1, language = Language.ENGLISH.code)
        }

    }

    test("get episode translations success") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.translations)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val result = apiConfig.api.getEpisodeTranslations(31910L, 1, 1)

        result.translations.isNotEmpty() shouldBe true
        result.translations.any { it.language == Language.ENGLISH.code } shouldBe true

    }

    test("get episode translations fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        shouldThrow<Exception> {
            apiConfig.api.getEpisodeTranslations(0L, 1, 1)
        }

    }

    test("get movie images maps backdrops and posters and ignores logos") {

        val mockResponse = MockResponse()
            .setBody(TMDBResponseMockups.movieImages)
            .setResponseCode(200)
        apiConfig.mockWebServer.enqueue(mockResponse)

        val result = apiConfig.api.getMovieImages(372058L, language = Language.ENGLISH.code).getOrThrow()

        result.backdrops.map { it.filePath } shouldBe listOf("/backdrop1.jpg", "/backdrop2.jpg")
        result.posters.map { it.filePath } shouldBe listOf("/poster1.jpg")
        result.backdrops.first().voteAverage shouldBe 5.3
        result.posters.first().aspectRatio shouldBe 0.667

        // The server is shared by all tests: the request of this test is the last one recorded
        val requests = generateSequence { apiConfig.mockWebServer.takeRequest(10, TimeUnit.MILLISECONDS) }.toList()
        requests.last().path shouldBe "/movie/372058/images?language=${Language.ENGLISH.code}"

    }

    test("get movie images fail") {

        val mockResponse = MockResponse()
            .setResponseCode(404)
        apiConfig.mockWebServer.enqueue(mockResponse)

        apiConfig.api.getMovieImages(0L, language = Language.ENGLISH.code).isFailure shouldBe true

    }

})