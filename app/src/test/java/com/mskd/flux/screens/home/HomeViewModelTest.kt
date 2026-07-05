package com.mskd.flux.screens.home

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.data.datastore.SnackbarDataStore
import com.mskd.flux.core.data.datastore.TokenDataStore
import com.mskd.flux.core.data.datastore.UserDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.core.domain.model.core.AppInfo
import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.ContentType
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.mockups.core.FakeDatabaseRepository
import com.mskd.flux.mockups.core.datastore.FakeSnackbarDataStore
import com.mskd.flux.mockups.features.catalog.FakeSyncCatalogUseCase
import com.mskd.flux.screen.home.HomeEvent
import com.mskd.flux.screen.home.HomeIntent
import com.mskd.flux.screen.home.HomeState
import com.mskd.flux.screen.home.HomeViewModel
import com.mskd.flux.utils.FluxSnackbar
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: HomeViewModel
    lateinit var syncCatalogUseCase: SyncCatalogUseCase
    lateinit var cleanCatalogUseCase: CleanCatalogUseCase
    lateinit var database: DatabaseRepository
    lateinit var userDataStore: UserDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var snackbarDataStore: SnackbarDataStore
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
        cleanCatalogUseCase = mockk(relaxed = true)
        database = FakeDatabaseRepository()

        snackbarDataStore = FakeSnackbarDataStore()

        appInfo = AppInfo(
            versionCode = 0,
            versionName = "Version-Test"
        )

    }

    context("initial state") {
        withData(
            nameFn = { it.description },
            HomeTestCases.InitialState(
                description = "without token",
                tokenValue = "",
                expectedSnackbarState = FluxSnackbar.Token
            ),
            HomeTestCases.InitialState(
                description = "with token",
                tokenValue = "token",
                expectedSnackbarState = FluxSnackbar.Tutorial
            )
        ) { testCase ->

            tokenFlow.value = testCase.tokenValue

            viewModel = HomeViewModel(
                syncCatalogUseCase = syncCatalogUseCase,
                cleanCatalogUseCase = cleanCatalogUseCase,
                database = database,
                tokenDataStore = tokenDataStore,
                userDataStore = userDataStore,
                snackbarDataStore = snackbarDataStore,
                appInfo = appInfo
            )

            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.state shouldBe HomeState.Content(
                    artworks = MediaMockups.artworks,
                    lastWatchedMediaIds = emptyList(),
                    isRefreshing = false
                )
                initialState.snackbarState shouldBe testCase.expectedSnackbarState

                cancelAndConsumeRemainingEvents()
            }

        }
    }

    test("should force sync when manual sync requested") {

        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.handleIntent(HomeIntent.SyncCatalog)

        coVerify {
            syncCatalogUseCase(onlyNew = true)
        }
    }


    test("should sync when last sync was more than 1 day ago") {
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns oldTime

        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        coVerify(exactly = 1) {
            syncCatalogUseCase(onlyNew = true)
        }
    }

    test("should not sync when last sync was less than 1 day ago") {
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime

        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        coVerify(exactly = 0) {
            syncCatalogUseCase(any())
        }
    }

    test("should sync when new app version") {

        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime

        appInfo = AppInfo(
            versionCode = Int.MAX_VALUE,
            versionName = "VersionTest"
        )

        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        coVerify(exactly = 1) {
            syncCatalogUseCase(onlyNew = false)
        }
    }

    test("on artwork show tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnArtworkTap(artwork = MediaMockups.showArtwork, rgb = 0x112233))
            awaitItem() shouldBe HomeEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = 0x112233)
        }
    }

    test("on artwork movie tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnArtworkTap(artwork = MediaMockups.movieArtwork, rgb = 0x112233))
            awaitItem() shouldBe HomeEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = 0x112233)
        }
    }

    test("on unknown artwork tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnArtworkTap(artwork = Artwork.UNKNOWN, rgb = null))
            awaitItem() shouldBe HomeEvent.NavigateToUnknown
        }
    }

    test("on category tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnCategoryTap(category = ContentType.MOVIE))
            awaitItem() shouldBe HomeEvent.NavigateToCategory(category = ContentType.MOVIE)
        }
    }

    test("on search tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnSearchTap)
            awaitItem() shouldBe HomeEvent.NavigateToSearch
        }
    }

    test("on settings tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnSettingsTap)
            awaitItem() shouldBe HomeEvent.NavigateToSettings
        }
    }

    test("on how to tap") {
        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnHowToTap)
            awaitItem() shouldBe HomeEvent.NavigateToHowTo
        }
    }

    test("on dismiss snackbar and snackbar action tap") {
        tokenFlow.value = ""

        viewModel = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.uiState.test {
            val stateWithSnackbar = awaitItem()
            stateWithSnackbar.snackbarState shouldBe FluxSnackbar.Token

            viewModel.handleIntent(HomeIntent.OnDismissSnackbar)
            val stateWithoutSnackbar = awaitItem()
            stateWithoutSnackbar.snackbarState shouldBe null

            cancelAndConsumeRemainingEvents()
        }

        tokenFlow.value = "token"

        val viewModelTutorial = HomeViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModelTutorial.uiState.test {
            awaitItem().snackbarState shouldBe FluxSnackbar.Tutorial

            viewModelTutorial.event.test {
                viewModelTutorial.handleIntent(HomeIntent.OnSnackbarActionTap)
                awaitItem() shouldBe HomeEvent.NavigateToHowTo
            }

            cancelAndConsumeRemainingEvents()
        }
    }

})