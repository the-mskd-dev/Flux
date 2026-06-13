package com.mskd.flux.data.repository

import androidx.test.core.app.ApplicationProvider
import com.mskd.flux.BuildConfig
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.data.tmdb.token.TokenRepository
import com.mskd.flux.di.dataStoreModule
import com.mskd.flux.di.ktorModule
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.tmdb.TMDBTranslations
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.util.Locale

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TmdbRepositoryImplTest : KoinTest {

    private val tmdbService: TMDBService by inject()
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
        stopKoin()
        val apiKey = BuildConfig.TMDB_TOKEN

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(
                ktorModule,
                dataStoreModule
            )
        }

        val tokenRepository: TokenRepository = get()
        runBlocking {
            tokenRepository.saveToken(BuildConfig.TMDB_TOKEN)
        }

        settingsRepository = mockk(relaxed = true) {
            coEvery { getDataLanguage() } returns dataLanguage
        }

        repository = TmdbRepositoryImpl(tmdbService, settingsRepository)

    }

    @After
    fun tearDown() {
        stopKoin()
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
        println("releaseDate : ${result?.releaseDate}")
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
        println("releaseDate : ${result?.releaseDate}")
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
        println("releaseDate : ${result?.releaseDate}")
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