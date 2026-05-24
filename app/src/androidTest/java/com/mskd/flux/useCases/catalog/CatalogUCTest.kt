package com.mskd.flux.useCases.catalog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mskd.flux.BuildConfig
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.FileSource
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.useCases.images.ImagesUC
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
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
class CatalogUCTest {

    private lateinit var tmdbService: TMDBService
    private lateinit var tmdbRepository: TmdbRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var databaseRepository: DatabaseRepository
    private lateinit var filesRepository: FilesRepository
    private lateinit var userRepository: UserRepository
    private lateinit var imagesUC: ImagesUC
    private lateinit var context: Context

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
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
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
            every { flow } returns kotlinx.coroutines.flow.flowOf(SettingsRepository.State())
        }

        tmdbRepository = TmdbRepositoryImpl(tmdbService, settingsRepository)

        databaseRepository = mockk(relaxed = true) {
            coEvery { saveMovies(any()) } answers {
                println("Saved Movies:")
                firstArg<List<Movie>>().forEach { println(it) }
            }
            coEvery { saveEpisodes(any()) } answers {
                println("Saved Episodes:")
                firstArg<List<Episode>>().forEach { println(it) }
            }
            coEvery { saveArtworks(any()) } answers {
                println("Saved Artworks:")
                firstArg<List<Artwork>>().forEach { println(it) }
            }
        }
        
        filesRepository = mockk(relaxed = true) {
            coEvery { getFiles() } returns listOf(movieFile, episodeFile)
            coEvery { filterExistingFiles(any()) } returns emptyList()
        }
        
        userRepository = mockk(relaxed = true)
        imagesUC = mockk(relaxed = true)
    }

    @Test
    fun test_1_sync_catalog() = runTest {
        val catalogUC = CatalogUCImpl(
            tmdb = tmdbRepository,
            database = databaseRepository,
            files = filesRepository,
            user = userRepository,
            settings = settingsRepository,
            imagesUC = imagesUC,
            scope = this,
            context = context
        )

        catalogUC.syncCatalog(onlyNew = false)

        catalogUC.state.first { it is CatalogUC.State.Syncing }
        catalogUC.state.first { it is CatalogUC.State.Idle }
    }

    @Test
    fun test_2_get_catalog() = runTest {

        val catalogUC = CatalogUCImpl(
            tmdb = tmdbRepository,
            database = databaseRepository,
            files = filesRepository,
            user = userRepository,
            settings = settingsRepository,
            imagesUC = imagesUC,
            scope = this,
            context = context
        )

        val catalog = catalogUC.getCatalog(listOf(movieFile, episodeFile)) {}

        assert(catalog.artworks.size == 2)
        assert(catalog.movies.size == 1)
        assert(catalog.episodes.size == 1)
    }

    @Test
    fun test_3_update_language() = runTest {
        coEvery { databaseRepository.getMovies() } returns listOf(
            Movie(
                artworkId = 372058L,
                title = "Spider-Man: Homecoming",
                releaseDateString = "2017",
                description = "",
                voteAverage = 0f,
                voteCount = 0,
                duration = 0,
                file = movieFile
            )
        )
        coEvery { databaseRepository.getEpisodes() } returns listOf(
            Episode(
                id = 1L,
                number = 1,
                season = 1,
                imagePath = "",
                artworkId = 31910L,
                title = "Naruto",
                releaseDateString = "",
                description = "",
                duration = 0,
                voteAverage = 0f,
                voteCount = 0,
                file = episodeFile
            )
        )

        val catalogUC = CatalogUCImpl(
            tmdb = tmdbRepository,
            database = databaseRepository,
            files = filesRepository,
            user = userRepository,
            settings = settingsRepository,
            imagesUC = imagesUC,
            scope = this,
            context = context
        )

        catalogUC.updateLanguage()

        catalogUC.state.first { it is CatalogUC.State.Syncing }
        catalogUC.state.first { it is CatalogUC.State.Idle }
    }
    
    @Test
    fun test_4_clean_catalog() = runTest {
        val catalogUC = CatalogUCImpl(
            tmdb = tmdbRepository,
            database = databaseRepository,
            files = filesRepository,
            user = userRepository,
            settings = settingsRepository,
            imagesUC = imagesUC,
            scope = this,
            context = context
        )

        catalogUC.cleanCatalog()
        
        // cleanCatalog is a suspend function, so it will finish when done.
        println("Catalog cleaned.")
    }
}
