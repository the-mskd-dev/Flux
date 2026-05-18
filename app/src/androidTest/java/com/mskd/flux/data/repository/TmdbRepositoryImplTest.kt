package com.mskd.flux.data.repository

import com.mskd.flux.BuildConfig
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.FileProperties
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.internal.MethodSorter
import org.junit.runner.OrderWith
import org.junit.runner.manipulation.Alphanumeric
import org.junit.runner.manipulation.Ordering
import org.junit.runners.MethodSorters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TmdbRepositoryImplTest {

    private lateinit var tmdbService: TMDBService
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: TmdbRepositoryImpl

    private val dataLanguage = Locale.US

    private val movieFile = UserFile(
        name = "Spider-man Homecoming",
        addedDateTime = 0L,
        path = "",
        source = FileSource.LOCAL
    )

    private val episodeFile = UserFile(
        name = "Naruto s01e01.mp4",
        addedDateTime = 0L,
        path = "",
        source = FileSource.LOCAL
    )

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

        tmdbService = retrofit.create(TMDBService::class.java)
        settingsRepository = mockk(relaxed = true) {
            coEvery { getDataLanguage() } returns dataLanguage
        }

        repository = TmdbRepositoryImpl(tmdbService, settingsRepository)

    }

    @Test
    fun test_1_get_tmdb_artwork_movie() = runTest {
        val result = repository.getTmdbArtwork(movieFile)

        println("id : ${result?.id}")
        println("title : ${result?.title}")
        println("description : ${result?.description}")
        println("type : ${result?.type}")
        println("imagePath : ${result?.imagePath}")
        println("bannerPath : ${result?.bannerPath}")
        println("originalTitle : ${result?.originalTitle}")
        println("popularity : ${result?.popularity}")
        println("releaseDateString : ${result?.releaseDateString}")
        println("voteCount : ${result?.voteCount}")
        println("voteAverage : ${result?.voteAverage}")
    }

    @Test
    fun test_2_get_tmdb_artwork_episode() = runTest {
        val result = repository.getTmdbArtwork(episodeFile)

        println("id : ${result?.id}")
        println("title : ${result?.title}")
        println("description : ${result?.description}")
        println("type : ${result?.type}")
        println("imagePath : ${result?.imagePath}")
        println("bannerPath : ${result?.bannerPath}")
        println("originalTitle : ${result?.originalTitle}")
        println("popularity : ${result?.popularity}")
        println("releaseDateString : ${result?.releaseDateString}")
        println("voteCount : ${result?.voteCount}")
        println("voteAverage : ${result?.voteAverage}")
    }

    @Test
    fun test_3_get_tmdb_movie() = runTest {
        val id = 1L // ID from TMDBServiceTest

        val result = repository.getTmdbMovie(artworkId = id)

        println("id : ${result?.id}")
        println("title : ${result?.title}")
        println("description : ${result?.description}")
        println("imagePath : ${result?.imagePath}")
        println("bannerPath : ${result?.bannerPath}")
        println("releaseDateString : ${result?.releaseDateString}")
        println("voteCount : ${result?.voteCount}")
        println("voteAverage : ${result?.voteAverage}")
    }

    @Test
    fun test_4_get_tmdb_episode() = runTest {
        val id = 31910L
        val season = 1
        val episode = 1

        val result = repository.getTmdbEpisode(
            artworkId = id,
            season = season,
            number = episode
        )

        println("id : ${result?.id}")
        println("artworkId : ${result?.artworkId}")
        println("title : ${result?.title}")
        println("description : ${result?.description}")
        println("imagePath : ${result?.imagePath}")
        println("releaseDateString : ${result?.releaseDateString}")
        println("season : ${result?.season}")
        println("number : ${result?.number}")
        println("voteCount : ${result?.voteCount}")
        println("voteAverage : ${result?.voteAverage}")
    }

    @Test
    fun test_5_get_tmdb_movie_translations() = runTest {
        val id = 372058L

        val result = repository.getTmdbMovieTranslations(artworkId = id)

        println("Result count : ${result.size}")
    }

    @Test
    fun test_6_get_tmdb_show_translations() = runTest {
        val id = 31910L

        val result = repository.getTmdbShowTranslations(artworkId = id)

        println("Result count : ${result.size}")
    }

    @Test
    fun test_7_get_tmdb_episode_translations() = runTest {
        val id = 31910L
        val season = 1
        val episode = 1

        val result = repository.getTmdbEpisodeTranslations(
            artworkId = id,
            season = season,
            number = episode
        )

        println("Result count : ${result.size}")
    }

}