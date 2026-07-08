package com.mskd.flux.useCases.catalog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mskd.flux.BuildConfig
import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.domain.datastore.SettingsDataStore
import com.mskd.flux.core.domain.datastore.TokenDataStore
import com.mskd.flux.core.domain.datastore.UserDataStore
import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.Episode
import com.mskd.flux.core.domain.model.artwork.Movie
import com.mskd.flux.core.domain.model.core.AppInfo
import com.mskd.flux.core.domain.model.files.FileSource
import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.di.Qualifiers
import com.mskd.flux.di.moduleAndroidApp
import com.mskd.flux.di.modulePlatform
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.features.tmdb.data.datasource.TmdbDataSourceImpl
import com.mskd.flux.features.tmdb.data.service.TMDBService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import java.util.Locale

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CatalogUCTest : KoinTest {

    private val tmdbService: TMDBService by inject()
    private lateinit var tmdbDataSource: TmdbDataSource
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var databaseRepository: DatabaseRepository
    private lateinit var getDeviceFilesUseCase: GetDeviceFilesUseCase
    private lateinit var filterExistingFilesUseCase: FilterExistingFilesUseCase
    private lateinit var userDataStore: UserDataStore
    private lateinit var imagesPrefetchManager: ImagesPrefetchManager
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

        val appInfo = AppInfo(
            versionCode = 0,
            versionName = "0",
            isDebug = true,
            debugToken = BuildConfig.TMDB_TOKEN
        )
    }

    @Before
    fun setup() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()

        startKoin {
            androidContext(context)
            module { single<AppInfo> { appInfo } }
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
            every { flow } returns flowOf(SettingsDataStore.State())
        }

        tmdbDataSource = TmdbDataSourceImpl(tmdbService, settingsDataStore)

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
        
        getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>(relaxed = true)
        coEvery { getDeviceFilesUseCase.invoke() } returns listOf(movieFile, episodeFile)
        
        filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>(relaxed = true)
        coEvery { filterExistingFilesUseCase.invoke(any()) } returns emptyList()
        
        userDataStore = mockk(relaxed = true)
        imagesPrefetchManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_1_sync_catalog() = runTest {
        val catalogUC = CatalogUCImpl(
            tmdb = tmdbDataSource,
            database = databaseRepository,
            getFilesUseCase = getDeviceFilesUseCase,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            user = userDataStore,
            settings = settingsDataStore,
            imagesPrefetchManager = imagesPrefetchManager,
            scope = this,
            appInfo = get(),
            metadataProvider = get(),
            dispatcher = get(Qualifiers.DEFAULT_DISPATCHER)
        )

        catalogUC.syncCatalog(onlyNew = false)

        catalogUC.state.first { it is CatalogUC.State.Syncing }
        catalogUC.state.first { it is CatalogUC.State.Idle }
    }

    @Test
    fun test_2_get_catalog() = runTest {

        val catalogUC = CatalogUCImpl(
            tmdb = tmdbDataSource,
            database = databaseRepository,
            getFilesUseCase = getDeviceFilesUseCase,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            user = userDataStore,
            settings = settingsDataStore,
            imagesPrefetchManager = imagesPrefetchManager,
            scope = this,
            appInfo = get(),
            metadataProvider = get(),
            dispatcher = get(Qualifiers.DEFAULT_DISPATCHER)
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
            tmdb = tmdbDataSource,
            database = databaseRepository,
            getFilesUseCase = getDeviceFilesUseCase,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            user = userDataStore,
            settings = settingsDataStore,
            imagesPrefetchManager = imagesPrefetchManager,
            scope = this,
            appInfo = get(),
            metadataProvider = get(),
            dispatcher = get(Qualifiers.DEFAULT_DISPATCHER)
        )

        catalogUC.updateLanguage()

        catalogUC.state.first { it is CatalogUC.State.Syncing }
        catalogUC.state.first { it is CatalogUC.State.Idle }
    }
    
    @Test
    fun test_4_clean_catalog() = runTest {
        val catalogUC = CatalogUCImpl(
            tmdb = tmdbDataSource,
            database = databaseRepository,
            getFilesUseCase = getDeviceFilesUseCase,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            user = userDataStore,
            settings = settingsDataStore,
            imagesPrefetchManager = imagesPrefetchManager,
            scope = this,
            appInfo = get(),
            metadataProvider = get(),
            dispatcher = get(Qualifiers.DEFAULT_DISPATCHER)
        )

        catalogUC.cleanCatalog()
        
        // cleanCatalog is a suspend function, so it will finish when done.
        println("Catalog cleaned.")
    }
}
