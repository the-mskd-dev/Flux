package com.mskd.flux.data.tmdb

import com.mskd.flux.BuildConfig
import com.mskd.flux.utils.extensions.toTmdbFormat
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Locale
import java.util.Properties

class TMDBServiceTest {

    private lateinit var service: TMDBService
    private val language = Locale.US.toTmdbFormat()

    @Before
    fun setup() {

        val apiKey = BuildConfig.TMDB_TOKEN

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(TMDBService::class.java)
    }

    @Test
    fun test_authenticate() = runTest {
        val result = service.authenticate()
        println(result)
    }

    @Test
    fun test_get_movie() = runTest {

        val title = ""
        val year: Int? = null

        val result = service.getMovie(
            title = title,
            year = year,
            language = language
        )

        println(result)

    }

    @Test
    fun test_get_movie_details() = runTest {

        val id = 1L

        val result = service.getMovieDetails(
            id = id,
            language = language
        )

        println(result)

    }

    @Test
    fun test_get_movie_translations() = runTest {

        val id = 372058L

        val result = service.getMovieTranslations(id = id)

        println(result)

    }

    @Test
    fun test_get_show() = runTest {

        val title = ""
        val year: Int? = null

        val result = service.getShow(
            title = title,
            year = year,
            language = language
        )

        println(result)

    }

    @Test
    fun test_get_show_translations() = runTest {

        val id = 31910L

        val result = service.getShowTranslations(id = id)

        println(result)

    }

    @Test
    fun test_get_episode() = runTest {

        val id = 31910L
        val season = 1
        val episode = 1

        val result = service.getEpisode(
            id = id,
            season = season,
            episode = episode,
            language = language
        )

        println(result)

    }

    @Test
    fun test_get_episode_translations() = runTest {

        val id = 31910L
        val season = 1
        val episode = 1

        val result = service.getEpisodeTranslations(
            id = id,
            season = season,
            episode = episode
        )

        println(result)

    }

}