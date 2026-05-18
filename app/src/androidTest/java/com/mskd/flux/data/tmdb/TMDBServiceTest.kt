package com.mskd.flux.data.tmdb

import com.mskd.flux.BuildConfig
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import com.mskd.flux.utils.extensions.toTmdbFormat
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TMDBServiceTest {

    private lateinit var service: TMDBService

    private companion object {

        val dataLanguage: String = Locale.US.toTmdbFormat()

        val movieFile = UserFile(
            name = "Spider-man Homecoming",
            addedDateTime = 0L,
            path = "",
            source = FileSource.LOCAL
        )

        val episodeFile = UserFile(
            name = "Naruto s01e01.mp4",
            addedDateTime = 0L,
            path = "",
            source = FileSource.LOCAL
        )
        private var movieArtworkId: Long? = null
        private var showArtworkId: Long? = null
    }

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
    fun test_1_authenticate() = runTest {
        val result = service.authenticate()

        println("Authentication success - ${result.success}")
        println("Message - ${result.status_message}")
        println("Code - ${result.status_code}")
    }

    @Test
    fun test_2_get_movie() = runTest {

        val title = movieFile.nameProperties.title
        val year = movieFile.nameProperties.year

        val result = service.getMovie(
            title = title,
            year = year,
            language = dataLanguage
        )

        movieArtworkId = result.artworkFor(fileName = movieFile.nameProperties.title)?.id
        println("Result count : ${result.resultCount}")
        result.results.forEach {
            println(it)
        }

    }

    @Test
    fun test_3_get_movie_details() = runTest {

        val id = movieArtworkId!!

        val result = service.getMovieDetails(
            id = id,
            language = dataLanguage
        )

        println(result)
    }

    @Test
    fun test_4_get_movie_translations() = runTest {

        val id = movieArtworkId!!

        val result = service.getMovieTranslations(id = id)

        println("Result count : ${result.translations.size}")

    }

    @Test
    fun test_5_get_show() = runTest {

        val title = episodeFile.nameProperties.title
        val year = episodeFile.nameProperties.year

        val result = service.getShow(
            title = title,
            year = year,
            language = dataLanguage
        )

        showArtworkId = result.artworkFor(fileName = episodeFile.nameProperties.title)?.id
        println("Result count : ${result.resultCount}")
        result.results.forEach {
            println(it)
        }

    }

    @Test
    fun test_6_get_show_translations() = runTest {

        val id = showArtworkId!!

        val result = service.getShowTranslations(id = id)

        println("Result count : ${result.translations.size}")

    }

    @Test
    fun test_7_get_episode() = runTest {

        val id = showArtworkId!!
        val season = episodeFile.nameProperties.season!!
        val episode = episodeFile.nameProperties.episode!!

        val result = service.getEpisode(
            id = id,
            season = season,
            episode = episode,
            language = dataLanguage
        )

        println(result)

    }

    @Test
    fun test_8_get_episode_translations() = runTest {

        val id = showArtworkId!!
        val season = episodeFile.nameProperties.season!!
        val episode = episodeFile.nameProperties.episode!!

        val result = service.getEpisodeTranslations(
            id = id,
            season = season,
            episode = episode
        )

        println("Result count : ${result.translations.size}")

    }

}