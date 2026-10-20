package com.mskd.flux.features.catalog.usecase

import app.cash.turbine.test
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.catalog.Catalog
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.catalog.domain.fetcher.CatalogContentFetcher
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncGenres.SyncGenresUseCase
import com.mskd.flux.features.catalog.fake.FakeCatalogSyncCoordinator
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.people.domain.usecase.SyncPeopleUseCase
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SyncCatalogUseCaseTest : FunSpec({

    val testDispatcher = StandardTestDispatcher()
    lateinit var testScope: TestScope

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
    }
    afterTest { Dispatchers.resetMain() }

    fun createUseCase(
        database: DatabaseRepository = mockk(relaxed = true),
        privateFolder: PrivateFolderDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(PrivateFolderDataStore.State())
        },
        user: UserDataStore = mockk(relaxed = true),
        imagesPrefetchManager: ImagesPrefetchManager = mockk(relaxed = true),
        appInfo: AppInfo = mockk(relaxed = true),
        coordinator: FakeCatalogSyncCoordinator = FakeCatalogSyncCoordinator(scope = testScope),
        getDeviceFilesUseCase: GetDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>().also {
            coEvery { it() } returns emptyList()
        },
        filterExistingFilesUseCase: FilterExistingFilesUseCase = mockk(relaxed = true),
        syncGenresUseCase: SyncGenresUseCase = mockk(relaxed = true),
        catalogFetcher: CatalogContentFetcher = mockk(relaxed = true),
        syncPeopleUseCase: SyncPeopleUseCase = mockk(relaxed = true),
    ) = SyncCatalogUseCase(
        database = database,
        privateFolder = privateFolder,
        user = user,
        imagesPrefetchManager = imagesPrefetchManager,
        appInfo = appInfo,
        coordinator = coordinator,
        getDeviceFilesUseCase = getDeviceFilesUseCase,
        filterExistingFilesUseCase = filterExistingFilesUseCase,
        syncGenresUseCase = syncGenresUseCase,
        catalogFetcher = catalogFetcher,
        syncPeopleUseCase = syncPeopleUseCase,
    )

    test("if full sync is running, nothing to do when a light sync is requested") {
        val coordinator = FakeCatalogSyncCoordinator(
            scope = testScope,
            initialState = SyncState.Syncing(full = true)
        )
        val useCase = createUseCase(
            coordinator = coordinator,
        )

        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 0
    }

    test("if full sync is running, re launch full sync if requested") {
        val coordinator = FakeCatalogSyncCoordinator(
            scope = testScope,
            initialState = SyncState.Syncing(full = true)
        )

        val useCase = createUseCase(coordinator = coordinator)

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 1
    }

    test("if light sync is running, launch full sync if requested") {
        val coordinator = FakeCatalogSyncCoordinator(
            scope = testScope,
            initialState = SyncState.Syncing(full = false)
        )

        val useCase = createUseCase(coordinator = coordinator)

        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 1
    }

    test("if light sync is running, re launch light sync if requested") {
        val coordinator = FakeCatalogSyncCoordinator(
            scope = testScope,
            initialState = SyncState.Syncing(full = false)
        )

        val useCase = createUseCase(coordinator = coordinator)

        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coordinator.launchCallCount shouldBe 1
    }

    test("if no new file, no sync, just a clean") {
        val database = mockk<DatabaseRepository>(relaxed = true)
        val user = mockk<UserDataStore>(relaxed = true)
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList()

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

    test("if light sync, exclude files already in database") {
        val existingFile = UserFile(name = "existing.mkv", path = "path/existing", addedDateTime = 0L, source = FileSource.LOCAL)
        val existingMovie = mockk<Movie>(relaxed = true) { every { file } returns existingFile }

        val database = mockk<DatabaseRepository>(relaxed = true)
        coEvery { database.getMedias() } returns listOf(existingMovie)

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

        coVerify { database.deleteMediasNotInFiles(listOf(existingFile)) }
        coVerify(exactly = 0) { database.deleteAll() }
    }

    test("if full sync, delete all database and save data from all files") {

        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { catalogFetcher.fetch(folders = any(), onProgress = any()) }
        coVerify { database.saveArtworks(newCatalog.artworks) }
        coVerify { database.saveSeasons(newCatalog.seasons) }
        coVerify { database.saveMedias(newCatalog.movies + newCatalog.episodes) }
        coVerify { database.deleteAll() }
    }

    test("files named with a TMDB ID are passed to the fetcher with their tmdbId") {
        // Given
        val deviceFiles = listOf(
            UserFile(name = "Dune {tmdb-438631} (2021).mkv", path = "path/dune"),
            UserFile(name = "Game of Thrones {tmdb-1399} S01E01.mkv", path = "path/got"),
            UserFile(name = "Inception.mkv", path = "path/inception"),
        )
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns deviceFiles

        val foldersSlot = slot<List<CatalogFolder>>()
        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(capture(foldersSlot), any()) } returns Catalog(
                artworks = emptyList(),
                movies = emptyList(),
                seasons = emptyList(),
                episodes = emptyList()
            )
        }

        val useCase = createUseCase(
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.state.test {
            awaitItem() shouldBe SyncState.Idle

            useCase.invoke(onlyNew = false)
            awaitItem() shouldBe SyncState.Syncing(full = true)

            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() shouldBe SyncState.Idle
        }

        // Then
        val folders = foldersSlot.captured
        folders.size shouldBe 3
        folders.map { it.tmdbId }.toSet() shouldBe setOf(438631L, 1399L, null)
    }

    test("files sharing a TMDB ID in differently named folders are passed as a single folder") {
        // Given
        val deviceFiles = listOf(
            UserFile(name = "Dune.mkv", path = "path/1", realPath = "movies/Dune {tmdb-438631}/Dune.mkv"),
            UserFile(name = "Dune.2021.1080p.mkv", path = "path/2", realPath = "other/Dune Part One {tmdb-438631}/Dune.2021.1080p.mkv"),
        )
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns deviceFiles

        val foldersSlot = slot<List<CatalogFolder>>()
        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(capture(foldersSlot), any()) } returns Catalog(
                artworks = emptyList(),
                movies = emptyList(),
                seasons = emptyList(),
                episodes = emptyList()
            )
        }

        val useCase = createUseCase(
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        foldersSlot.captured.size shouldBe 1
        foldersSlot.captured.first().tmdbId shouldBe 438631L
        foldersSlot.captured.first().files shouldBe deviceFiles
    }

    test("the state changes from Idle to Syncing and then returns to Idle during execution") {
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

    test("if full sync, only private artworks keep their private flag") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val privateArtwork = MediaMockups.movieArtwork
        val publicArtwork = MediaMockups.showArtwork

        val database = mockk<DatabaseRepository>(relaxed = true)
        coEvery { database.getPrivateArtworkIds() } returns listOf(privateArtwork.id)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { database.getPrivateArtworkIds() }
        coVerify(exactly = 1) { database.setArtworkPrivate(artworkId = privateArtwork.id, isPrivate = true) }
        coVerify(exactly = 0) { database.setArtworkPrivate(artworkId = publicArtwork.id, isPrivate = true) }
    }

    test("if light sync, private flags are not restored") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { database.getPrivateArtworkIds() }
        coVerify(exactly = 0) { database.setArtworkPrivate(artworkId = any(), isPrivate = true) }
    }

    test("if private folder is enabled with nsfw included, nsfw artworks are marked as private") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)
        val privateFolder = mockk<PrivateFolderDataStore>(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PrivateFolderDataStore.State(enabled = true, includeNsfw = true)
            )
        }

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            privateFolder = privateFolder,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { database.setNsfwArtworksPrivate() }
    }

    test("if private folder is enabled without nsfw included, nsfw artworks are not marked as private") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)
        val privateFolder = mockk<PrivateFolderDataStore>(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PrivateFolderDataStore.State(enabled = true, includeNsfw = false)
            )
        }

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            privateFolder = privateFolder,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { database.setNsfwArtworksPrivate() }
    }

    test("if private folder is disabled, nsfw artworks are not marked as private") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)
        val privateFolder = mockk<PrivateFolderDataStore>(relaxed = true) {
            every { flow } returns MutableStateFlow(
                PrivateFolderDataStore.State(enabled = false, includeNsfw = true)
            )
        }

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            privateFolder = privateFolder,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { database.setNsfwArtworksPrivate() }
    }

    // endregion

    test("if the same media identity is duplicated in the catalog, it is saved only once") {
        // Given
        val duplicatedEpisode = MediaMockups.episode1
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes + duplicatedEpisode
        )
        val database = mockk<DatabaseRepository>(relaxed = true)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.episodes.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { database.saveMedias(newCatalog.movies + MediaMockups.episodes) }
    }

    // region SyncPeopleUseCase integration

    test("if new files are detected during light sync, people sync targets only the new artworks") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)
        val syncPeopleUseCase = mockk<SyncPeopleUseCase>(relaxed = true)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher,
            syncPeopleUseCase = syncPeopleUseCase,
        )

        // When
        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(exactly = 1) { syncPeopleUseCase(artworks = any(), force = false) }
        verify(exactly = 0) { syncPeopleUseCase(artworks = null, force = true) }
    }

    test("if new files are detected during full sync, people sync is forced") {
        // Given
        val newCatalog = Catalog(
            artworks = MediaMockups.artworks,
            movies = MediaMockups.movies,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodes
        )
        val database = mockk<DatabaseRepository>(relaxed = true)
        val syncPeopleUseCase = mockk<SyncPeopleUseCase>(relaxed = true)

        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns newCatalog.movies.map { it.file }

        val catalogFetcher = mockk<CatalogContentFetcher>(relaxed = true) {
            coEvery { fetch(any(), any()) } returns newCatalog
        }

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            catalogFetcher = catalogFetcher,
            syncPeopleUseCase = syncPeopleUseCase,
        )

        // When
        useCase.invoke(onlyNew = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(exactly = 1) { syncPeopleUseCase(force = true) }
        verify(exactly = 0) { syncPeopleUseCase(artworks = match { it != null }, force = any()) }
    }

    test("if light sync finds no new file, people sync only checks the 7-day period") {
        // Given
        val database = mockk<DatabaseRepository>(relaxed = true)
        val syncPeopleUseCase = mockk<SyncPeopleUseCase>(relaxed = true)
        val getDeviceFilesUseCase = mockk<GetDeviceFilesUseCase>()
        coEvery { getDeviceFilesUseCase() } returns emptyList()

        val useCase = createUseCase(
            database = database,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            syncPeopleUseCase = syncPeopleUseCase,
        )

        // When
        useCase.invoke(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { database.deleteMediasNotInFiles(emptyList()) }
        verify(exactly = 1) { syncPeopleUseCase(force = false) }
    }

    // endregion


})