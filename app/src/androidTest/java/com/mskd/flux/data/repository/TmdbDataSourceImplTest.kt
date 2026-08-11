package com.mskd.flux.data.repository

import androidx.test.core.app.ApplicationProvider
import com.mskd.flux.BuildConfig
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSourceImpl
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.core.network.tmdb.domain.model.TranslationRequest
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
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
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TmdbDataSourceImplTest : KoinTest {

    //region Setup

    private val tmdbService: TMDBService by inject()
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var repository: TmdbDataSourceImpl

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

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(
                modulePlatform,
                moduleAndroidApp
            )
        }

        val tokenDataStore: TokenDataStore = get()
        runBlocking {
            tokenDataStore.saveToken(BuildConfig.TMDB_TOKEN)
        }

        settingsDataStore = mockk(relaxed = true) {
            coEvery { getDataLanguage() } returns dataLanguage
        }

        repository = TmdbDataSourceImpl(tmdbService, settingsDataStore)

    }

    @After
    fun tearDown() {
        stopKoin()
    }

    //endregion

    //region Movie

    @Test
    fun test_0_get_movie_artwork() = runTest {
        val result = repository.getArtwork(movieFile)

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
    fun test_get_movie() = runTest {

        val result = repository.getMovie(artworkId = movieArtworkId!!)

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
    fun test_get_movie_translations() = runTest {

        val result = repository.getTranslation(
            request = TranslationRequest.Movie(
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

    //endregion

    //region Show

    @Test
    fun test_0_get_show_artwork() = runTest {

        // Given & When
        val result = repository.getArtwork(episodeFile)

        // Then
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
    fun test_get_show() = runTest {

        // Given
        val artworkId = showArtworkId!!

        // When
        val result = repository.getShow(artworkId = artworkId)

        // Then
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
    fun test_get_show_translations() = runTest {

        val result = repository.getTranslation(
            request = TranslationRequest.Show(
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

    //endregion

    //region Episode

    @Test
    fun test_get_episode() = runTest {

        val result = repository.getEpisode(
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
    fun test_get_episode_translations() = runTest {

        val result = repository.getTranslation(
            request = TranslationRequest.Episode(
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

    //endregion

    //region Season

    @Test
    fun test_get_season() = runTest {

        val result = repository.getSeason(
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
    fun test_get_season_translations() = runTest {

        val result = repository.getTranslation(
            request = TranslationRequest.Season(
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

    //endregion

    //region Genres

    @Test
    fun test_get_genres() = runTest {

        // Given & When
        val result = repository.getGenres()

        // Then
        assertTrue(result.isNotEmpty())
    }

    //endregion

}