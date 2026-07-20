package com.mskd.flux.data.tmdb

import androidx.test.core.app.ApplicationProvider
import com.mskd.flux.BuildConfig
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.service.TMDBService
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.utils.extensions.toTmdbFormat
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
class TMDBServiceTest : KoinTest {

    private val service: TMDBService by inject()

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

    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_01_authenticate() = runTest {
        val result = service.authenticate()

        println("Authentication success - ${result.success}")
        println("Message - ${result.message}")
        println("Code - ${result.code}")
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
        println("releaseDateString : ${result.releaseDate}")
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