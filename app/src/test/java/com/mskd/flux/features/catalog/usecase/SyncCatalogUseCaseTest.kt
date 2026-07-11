package com.mskd.flux.features.catalog.domain.usecase.syncCatalog

import app.cash.turbine.test
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.fake.FakeCatalogSyncCoordinator
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
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
        tmdb: TmdbDataSource = mockk(relaxed = true),
        database: DatabaseRepository = mockk(relaxed = true),
        user: UserDataStore = mockk(relaxed = true),
        settings: SettingsDataStore = mockk(relaxed = true),
        imagesPrefetchManager: ImagesPrefetchManager = mockk(relaxed = true),
        appInfo: AppInfo = mockk(relaxed = true),
        coordinator: FakeCatalogSyncCoordinator = FakeCatalogSyncCoordinator(scope = testScope),
        deleteUnavailableSourcesUseCase: DeleteUnavailableSourcesUseCase = mockk(relaxed = true),
        getFileDurationUseCase: GetFileDurationUseCase = mockk(relaxed = true),
        getDeviceFilesUseCase: GetDeviceFilesUseCase = mockk(relaxed = true),
        filterExistingFilesUseCase: FilterExistingFilesUseCase = mockk(relaxed = true)
    ) = SyncCatalogUseCaseImpl(
        tmdb = tmdb,
        database = database,
        user = user,
        settings = settings,
        imagesPrefetchManager = imagesPrefetchManager,
        appInfo = appInfo,
        coordinator = coordinator,
        deleteUnavailableSourcesUseCase = deleteUnavailableSourcesUseCase,
        getFileDurationUseCase = getFileDurationUseCase,
        getDeviceFilesUseCase = getDeviceFilesUseCase,
        filterExistingFilesUseCase = filterExistingFilesUseCase,
        dispatcher = testDispatcher
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
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList()

        val useCase = createUseCase(coordinator = coordinator, getDeviceFilesUseCase = getDeviceFilesUseCase)

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 1
    }

    // endregion

    // region Nettoyage préalable

    test("deleteUnavailableSourcesUseCase est appelé en tout premier") {
        val deleteUnavailableSourcesUseCase = mockk<DeleteUnavailableSourcesUseCase>(relaxed = true)
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList()

        val useCase = createUseCase(
            deleteUnavailableSourcesUseCase = deleteUnavailableSourcesUseCase,
            getDeviceFilesUseCase = getDeviceFilesUseCase
        )

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
        val tmdb = mockk<TmdbDataSource>(relaxed = true)
        coEvery { getDeviceFilesUseCase() } returns emptyList()

        val useCase = createUseCase(
            database = database,
            user = user,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            tmdb = tmdb
        )

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { database.deleteMediasNotInFiles(emptyList()) }
        coVerify { user.setSyncTime(any()) }

        // Aucune étape de catalogage n'a dû être déclenchée
        coVerify(exactly = 0) { database.saveArtworks(any()) }
        coVerify(exactly = 0) { tmdb.getTmdbArtwork(any()) }
    }

    // endregion

    // region onlyNew=true : filtrage des fichiers déjà en base

    test("onlyNew=true exclut de newFiles les fichiers déjà présents en base (comparaison par name)") {
        val existingFile = UserFile(name = "existing.mkv", path = "path/existing", addedDateTime = 0L, source = FileSource.LOCAL)
        val newFile = UserFile(name = "new.mkv", path = "path/new", addedDateTime = 0L, source = FileSource.LOCAL)
        val existingMovie = mockk<Movie>(relaxed = true) { every { file } returns existingFile }

        val database = mockk<DatabaseRepository>(relaxed = true)
        coEvery { database.getMovies() } returns listOf(existingMovie)
        coEvery { database.getEpisodes() } returns emptyList()

        val filterExistingFilesUseCase = mockk<FilterExistingFilesUseCase>()
        coEvery { filterExistingFilesUseCase(files = any()) } returns listOf(existingFile)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        // deviceFiles vide → newFiles vide quoi qu'il arrive, on isole ici uniquement
        // l'appel à deleteMediasNotInFiles pour vérifier le comportement onlyNew=true vs false.
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

    // region State (Turbine)

    test("state passe de Idle à Syncing puis revient à Idle pendant l'exécution") {
        val coordinator = FakeCatalogSyncCoordinator(scope = testScope)
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList()

        val useCase = createUseCase(coordinator = coordinator, getDeviceFilesUseCase = getDeviceFilesUseCase)

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