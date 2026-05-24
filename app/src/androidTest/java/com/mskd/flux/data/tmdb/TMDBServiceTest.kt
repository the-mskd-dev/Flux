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
    fun test_01_authenticate() = runTest {
        val result = service.authenticate()

        println("Authentication success - ${result.success}")
        println("Message - ${result.status_message}")
        println("Code - ${result.status_code}")
    }

    @Test
    fun test_02_get_movie() = runTest {

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
    fun test_03_get_movie_details() = runTest {

        val id = movieArtworkId!!

        val result = service.getMovieDetails(
            id = id,
            language = dataLanguage
        )

        println("id : ${result.id}")
        println("title : ${result.title}")
        println("description : ${result.description}")
        println("imagePath : ${result.imagePath}")
        println("bannerPath : ${result.bannerPath}")
        println("releaseDateString : ${result.releaseDateString}")
        println("voteCount : ${result.voteCount}")
        println("voteAverage : ${result.voteAverage}")
    }

    @Test
    fun test_04_get_movie_translations() = runTest {

        val id = movieArtworkId!!

        val result = service.getMovieTranslations(artworkId = id)

        println("Result count : ${result.translations.size}")

    }

    @Test
    fun test_05_get_show() = runTest {

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
            println("title : ${it.title}")
        }

    }

    @Test
    fun test_06_get_show_translations() = runTest {

        val id = showArtworkId!!

        val result = service.getShowTranslations(artworkId = id)

        println("Result count : ${result.translations.size}")

    }

    @Test
    fun test_07_get_episode() = runTest {

        val id = showArtworkId!!
        val season = episodeFile.nameProperties.season!!
        val episode = episodeFile.nameProperties.episode!!

        val result = service.getEpisode(
            id = id,
            season = season,
            number = episode,
            language = dataLanguage
        )

        println("id : ${result.id}")
        println("artworkId : ${result.artworkId}")
        println("title : ${result.title}")
        println("description : ${result.description}")
        println("imagePath : ${result.imagePath}")
        println("releaseDateString : ${result.releaseDateString}")
        println("season : ${result.season}")
        println("number : ${result.number}")
        println("voteCount : ${result.voteCount}")
        println("voteAverage : ${result.voteAverage}")

    }

    @Test
    fun test_08_get_episode_translations() = runTest {

        val id = showArtworkId!!
        val season = episodeFile.nameProperties.season!!
        val episode = episodeFile.nameProperties.episode!!

        val result = service.getEpisodeTranslations(
            artworkId = id,
            season = season,
            number = episode
        )

        println("Result count : ${result.translations.size}")

    }

    @Test
    fun test_09_get_season() = runTest {

        val id = showArtworkId!!
        val season = episodeFile.nameProperties.season!!

        val result = service.getSeason(
            id = id,
            season = season,
            language = dataLanguage
        )

        println("id : ${result.id}")
        println("title : ${result.title}")
        println("description : ${result.description}")
        println("imagePath : ${result.imagePath}")
        println("season : ${result.season}")
        println("number of episodes : ${result.episodes.size}")
    }

    @Test
    fun test_10_get_season_translations() = runTest {

        val id = showArtworkId!!
        val season = episodeFile.nameProperties.season!!

        val result = service.getSeasonTranslations(
            artworkId = id,
            season = season,
        )

        println("Result count : ${result.translations.size}")

    }

}