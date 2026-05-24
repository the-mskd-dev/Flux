package com.mskd.flux.data.repository

import com.mskd.flux.BuildConfig
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBTranslations
import io.mockk.coEvery
import io.mockk.mockk
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
class TmdbRepositoryImplTest {

    private lateinit var tmdbService: TMDBService
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var repository: TmdbRepositoryImpl

    private companion object {

        val dataLanguage: Locale = Locale.US

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

        tmdbService = retrofit.create(TMDBService::class.java)
        settingsRepository = mockk(relaxed = true) {
            coEvery { getDataLanguage() } returns dataLanguage
        }

        repository = TmdbRepositoryImpl(tmdbService, settingsRepository)

    }

    @Test
    fun test_01_get_tmdb_artwork_movie() = runTest {
        val result = repository.getTmdbArtwork(movieFile)

        assert(result != null)

        movieArtworkId = result?.id
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
    fun test_02_get_tmdb_movie() = runTest {

        val result = repository.getTmdbMovie(artworkId = movieArtworkId!!)

        assert(result != null)

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
    fun test_03_get_tmdb_movie_translations() = runTest {

        val result = repository.getTmdbTranslation(
            request = TMDBTranslations.Request.Movie(
                artworkId = movieArtworkId!!,
                language = dataLanguage
            ),
        )

        assert(result != null)

        println("name : ${result?.name}")
        println("english name : ${result?.englishName}")
        println("language : ${result?.language}")
        println("country : ${result?.country}")
    }

    @Test
    fun test_04_get_tmdb_artwork_show() = runTest {
        val result = repository.getTmdbArtwork(episodeFile)

        assert(result != null)

        showArtworkId = result?.id
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
    fun test_05_get_tmdb_show_translations() = runTest {

        val result = repository.getTmdbTranslation(
            request = TMDBTranslations.Request.Show(
                artworkId = showArtworkId!!,
                language = dataLanguage
            ),
        )

        assert(result != null)

        println("name : ${result?.name}")
        println("english name : ${result?.englishName}")
        println("language : ${result?.language}")
        println("country : ${result?.country}")
    }

    @Test
    fun test_06_get_tmdb_episode() = runTest {

        val result = repository.getTmdbEpisode(
            artworkId = showArtworkId!!,
            season = episodeFile.nameProperties.season!!,
            number = episodeFile.nameProperties.episode!!
        )

        assert(result != null)

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
    fun test_07_get_tmdb_episode_translations() = runTest {

        val result = repository.getTmdbTranslation(
            request = TMDBTranslations.Request.Episode(
                artworkId = showArtworkId!!,
                season = episodeFile.nameProperties.season!!,
                number = episodeFile.nameProperties.episode!!,
                language = dataLanguage
            ),
        )

        assert(result != null)

        println("name : ${result?.name}")
        println("english name : ${result?.englishName}")
        println("language : ${result?.language}")
        println("country : ${result?.country}")

    }

    @Test
    fun test_08_get_tmdb_season() = runTest {

        val result = repository.getTmdbSeason(
            artworkId = showArtworkId!!,
            season = episodeFile.nameProperties.season!!,
        )

        assert(result != null)

        println("id : ${result?.id}")
        println("title : ${result?.title}")
        println("description : ${result?.description}")
        println("imagePath : ${result?.imagePath}")
        println("season : ${result?.season}")
        println("number of episodes : ${result?.episodes?.size}")
    }

    @Test
    fun test_09_get_tmdb_season_translations() = runTest {

        val result = repository.getTmdbTranslation(
            request = TMDBTranslations.Request.Season(
                artworkId = showArtworkId!!,
                season = episodeFile.nameProperties.season!!,
                language = dataLanguage
            ),
        )

        assert(result != null)

        println("name : ${result?.name}")
        println("english name : ${result?.englishName}")
        println("language : ${result?.language}")
        println("country : ${result?.country}")

    }

}