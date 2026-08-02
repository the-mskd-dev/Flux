package com.mskd.flux.features.catalog.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.fake.FakeSyncCatalogUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: CatalogViewModel
    lateinit var syncCatalogUseCase: SyncCatalogUseCase
    lateinit var database: DatabaseRepository
    lateinit var userDataStore: UserDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var appInfo: AppInfo

    // Mocked flows
    val dataStoreFlow = MutableStateFlow(UserDataStore.State())
    val tokenFlow = MutableStateFlow("token")

    beforeTest {

        tokenDataStore = mockk(relaxed = true) {
            coEvery { flow } returns tokenFlow
        }
        userDataStore = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        syncCatalogUseCase = FakeSyncCatalogUseCase()
        database = FakeDatabaseRepository()

        appInfo = AppInfo(
            versionCode = 0,
            versionName = "Version-Test"
        )

    }

    fun createViewModel(syncUseCase: SyncCatalogUseCase = syncCatalogUseCase): CatalogViewModel {
        return CatalogViewModel(
            syncCatalogUseCase = syncUseCase,
            database = database,
            userDataStore = userDataStore,
            tokenDataStore = tokenDataStore,
            appInfo = appInfo
        )
    }

    test("initial state") {

        // Given & When
        viewModel = createViewModel()

        viewModel.uiState.test {

            // Then
            val initialState = awaitItem()
            initialState.state shouldBe CatalogState.Content(
                artworks = MediaMockups.artworks,
                lastWatchedMediaIds = emptyList(),
                isRefreshing = false
            )

            cancelAndConsumeRemainingEvents()
        }
    }

    test("should force sync when manual sync requested") {

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        viewModel.handleIntent(CatalogIntent.SyncCatalog)

        verify {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }


    test("should sync when last sync was more than 1 day ago") {

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns oldTime

        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }

    test("should sync when last sync was less than 1 day ago (delegated to usecase)") {

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime

        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }

    test("should sync when new app version") {

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime

        appInfo = AppInfo(
            versionCode = Int.MAX_VALUE,
            versionName = "VersionTest"
        )

        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = false)
        }
    }

    test("on artwork show tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = MediaMockups.showArtwork, rgb = 0x112233))
            awaitItem() shouldBe CatalogEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = 0x112233)
        }
    }

    test("on artwork movie tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = MediaMockups.movieArtwork, rgb = 0x112233))
            awaitItem() shouldBe CatalogEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = 0x112233)
        }
    }

    test("on unknown artwork tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN, rgb = null))
            awaitItem() shouldBe CatalogEvent.NavigateToUnknown
        }
    }

    test("on category tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnCategoryTap(category = ContentType.MOVIE))
            awaitItem() shouldBe CatalogEvent.NavigateToCategory(category = ContentType.MOVIE)
        }
    }

    test("on search tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnSearchTap)
            awaitItem() shouldBe CatalogEvent.NavigateToSearch
        }
    }

    test("on settings tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnSettingsTap)
            awaitItem() shouldBe CatalogEvent.NavigateToSettings
        }
    }

    test("on how to tap") {
        viewModel = createViewModel()

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnHowToTap)
            awaitItem() shouldBe CatalogEvent.NavigateToHowTo
        }
    }

})