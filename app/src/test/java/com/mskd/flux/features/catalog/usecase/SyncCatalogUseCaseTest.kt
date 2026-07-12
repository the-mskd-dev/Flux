package com.mskd.flux.features.catalog.usecase

import app.cash.turbine.test
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.catalog.domain.fetcher.ArtworkFolderFetcher
import com.mskd.flux.features.catalog.domain.fetcher.MovieMetadataFetcher
import com.mskd.flux.features.catalog.domain.fetcher.SeasonMetadataFetcher
import com.mskd.flux.features.catalog.domain.model.ArtworkFiles
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.resolver.EpisodeResolver
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCaseImpl
import com.mskd.flux.features.catalog.fake.FakeCatalogSyncCoordinator
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.sources.domain.usecase.DeleteUnavailableSourcesUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SyncCatalogUseCaseImplTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()
    lateinit var testScope: TestScope

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
    }
    afterTest { Dispatchers.resetMain() }

    fun createUseCase(
        database: DatabaseRepository = mockk(relaxed = true),
        user: UserDataStore = mockk(relaxed = true),
        imagesPrefetchManager: ImagesPrefetchManager = mockk(relaxed = true),
        appInfo: AppInfo = mockk(relaxed = true),
        coordinator: FakeCatalogSyncCoordinator = FakeCatalogSyncCoordinator(scope = testScope),
        deleteUnavailableSourcesUseCase: DeleteUnavailableSourcesUseCase = mockk(relaxed = true),
        getDeviceFilesUseCase: GetDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>().also {
            coEvery { it() } returns emptyList() // valeur par défaut sûre, écrasable via coEvery dans chaque test
        },
        filterExistingFilesUseCase: FilterExistingFilesUseCase = mockk(relaxed = true),
        artworkFolderFetcher: ArtworkFolderFetcher = mockk(relaxed = true),
        movieMetadataFetcher: MovieMetadataFetcher = mockk(relaxed = true),
        seasonMetadataFetcher: SeasonMetadataFetcher = mockk(relaxed = true),
        episodeResolver: EpisodeResolver = mockk(relaxed = true),
    ) = SyncCatalogUseCaseImpl(
        database = database,
        user = user,
        imagesPrefetchManager = imagesPrefetchManager,
        appInfo = appInfo,
        coordinator = coordinator,
        deleteUnavailableSourcesUseCase = deleteUnavailableSourcesUseCase,
        getDeviceFilesUseCase = getDeviceFilesUseCase,
        filterExistingFilesUseCase = filterExistingFilesUseCase,
        artworkFolderFetcher = artworkFolderFetcher,
        movieMetadataFetcher = movieMetadataFetcher,
        seasonMetadataFetcher = seasonMetadataFetcher,
        episodeResolver = episodeResolver,
    )

    // region Early return

    test("invoke ne déclenche rien si une synchro full est déjà en cours et onlyNew=true") {
        val coordinator = FakeCatalogSyncCoordinator(
            scope = testScope,
            initialState = SyncState.Syncing(full = true)
        )
        val deleteUnavailableSourcesUseCase = mockk<DeleteUnavailableSourcesUseCase>(relaxed = true)

        val useCase = createUseCase(
            coordinator = coordinator,
            deleteUnavailableSourcesUseCase = deleteUnavailableSourcesUseCase
        )

        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 0
        coVerify(exactly = 0) { deleteUnavailableSourcesUseCase() }
    }

    test("invoke se déclenche si une synchro full est en cours mais onlyNew=false") {
        val coordinator = FakeCatalogSyncCoordinator(
            scope = testScope,
            initialState = SyncState.Syncing(full = true)
        )

        val useCase = createUseCase(coordinator = coordinator)

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 1
    }

    // endregion

    // region Nettoyage préalable

    test("deleteUnavailableSourcesUseCase est appelé en tout premier") {
        val deleteUnavailableSourcesUseCase = mockk<DeleteUnavailableSourcesUseCase>(relaxed = true)

        val useCase = createUseCase(deleteUnavailableSourcesUseCase = deleteUnavailableSourcesUseCase)

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteUnavailableSourcesUseCase() }
    }

    // endregion

    // region Aucun nouveau fichier

    test("si getDeviceFilesUseCase ne retourne aucun fichier, la synchro s'arrête après le nettoyage") {
        val database = mockk<DatabaseRepository>(relaxed = true)
        val user = mockk<UserDataStore>(relaxed = true)
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList() // ← fix : stub explicite ajouté

        val useCase = createUseCase(
            database = database,
            user = user,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
        )

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { database.deleteMediasNotInFiles(emptyList()) }
        coVerify { user.setSyncTime(any()) }
        coVerify(exactly = 0) { database.saveArtworks(any()) }
    }

    // endregion

    // region onlyNew=true : filtrage des fichiers déjà en base

    test("onlyNew=true exclut de newFiles les fichiers déjà présents en base (comparaison par name)") {
        val existingFile = UserFile(name = "existing.mkv", path = "path/existing", addedDateTime = 0L, source = FileSource.LOCAL)
        val existingMovie = mockk<Movie>(relaxed = true) { every { file } returns existingFile }

        val database = mockk<DatabaseRepository>(relaxed = true)
        coEvery { database.getMovies() } returns listOf(existingMovie)
        coEvery { database.getEpisodes() } returns emptyList()

        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery { filterExistingFilesUseCase(files = any()) } returns listOf(existingFile)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList()

        val useCase = createUseCase(
            database = database,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            getDeviceFilesUseCase = getDeviceFilesUseCase
        )

        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { database.deleteMediasNotInFiles(emptyList()) }
        coVerify(exactly = 0) { database.deleteAll() }
    }

    // endregion

    // region Pipeline complet (nouveau)

    test("des nouveaux fichiers déclenchent le pipeline complet et sauvegardent le catalogue") {
        val newFile = UserFile(name = "movie1.mkv", path = "path/movie1", addedDateTime = 0L, source = FileSource.LOCAL)
        val artworkFiles = ArtworkFiles(artwork = Artwork(id = 42L), files = listOf(newFile))

        val database = mockk<DatabaseRepository>(relaxed = true)
        coEvery { database.getMovies() } returns emptyList()
        coEvery { database.getEpisodes() } returns emptyList()

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns listOf(newFile)

        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery { filterExistingFilesUseCase(files = any()) } returns emptyList()

        val artworkFolderFetcher = mockk<ArtworkFolderFetcher>()
        coEvery { artworkFolderFetcher.fetch(folders = any(), onProgress = any()) } returns listOf(artworkFiles)

        val movieMetadataFetcher = mockk<MovieMetadataFetcher>()
        coEvery { movieMetadataFetcher.fetch(artworkFiles = any(), onProgress = any()) } returns emptyList()

        val seasonMetadataFetcher = mockk<SeasonMetadataFetcher>()
        coEvery { seasonMetadataFetcher.fetch(artworkFiles = any()) } returns emptyList()

        val episodeResolver = mockk<EpisodeResolver>()
        coEvery { episodeResolver.resolve(artworkFiles = any(), episodesDto = any(), onProgress = any()) } returns emptyList()

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            artworkFolderFetcher = artworkFolderFetcher,
            movieMetadataFetcher = movieMetadataFetcher,
            seasonMetadataFetcher = seasonMetadataFetcher,
            episodeResolver = episodeResolver
        )

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { artworkFolderFetcher.fetch(folders = any(), onProgress = any()) }
        coVerify { database.saveArtworks(listOf(artworkFiles.artwork)) }
        coVerify { database.deleteAll() }
    }

    // endregion

    // region State (Turbine)

    test("state passe de Idle à Syncing puis revient à Idle pendant l'exécution") {
        val coordinator = FakeCatalogSyncCoordinator(scope = testScope)

        val useCase = createUseCase(coordinator = coordinator)

        useCase.state.test {
            awaitItem() shouldBe SyncState.Idle

            useCase.invoke(onlyNew = false)
            awaitItem() shouldBe SyncState.Syncing(full = true)

            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SyncState.Idle
        }
    }

    // endregion

})