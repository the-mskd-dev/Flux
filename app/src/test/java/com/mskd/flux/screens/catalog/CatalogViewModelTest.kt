package com.mskd.flux.screens.catalog

import app.cash.turbine.test
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.SnackbarDataStore
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.usecase.cleanCatalog.CleanCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.presentation.catalog.CatalogEvent
import com.mskd.flux.features.catalog.presentation.catalog.CatalogIntent
import com.mskd.flux.features.catalog.presentation.catalog.CatalogState
import com.mskd.flux.features.catalog.presentation.catalog.CatalogViewModel
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.core.FakeDatabaseRepository
import com.mskd.flux.mockups.core.datastore.FakeSnackbarDataStore
import com.mskd.flux.mockups.features.catalog.FakeSyncCatalogUseCase
import com.mskd.flux.utils.FluxSnackbar
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
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

    lateinit var viewModel: CatalogViewModel
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

            viewModel = CatalogViewModel(
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
                initialState.state shouldBe CatalogState.Content(
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

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.handleIntent(CatalogIntent.SyncCatalog)

        verify {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }


    test("should sync when last sync was more than 1 day ago") {

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns oldTime

        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }

    test("should not sync when last sync was less than 1 day ago") {

        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)

        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime

        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        verify(exactly = 0) {
            syncCatalogUseCaseSpy(any())
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

        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = false)
        }
    }

    test("on artwork show tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = MediaMockups.showArtwork, rgb = 0x112233))
            awaitItem() shouldBe CatalogEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = 0x112233)
        }
    }

    test("on artwork movie tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = MediaMockups.movieArtwork, rgb = 0x112233))
            awaitItem() shouldBe CatalogEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = 0x112233)
        }
    }

    test("on unknown artwork tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN, rgb = null))
            awaitItem() shouldBe CatalogEvent.NavigateToUnknown
        }
    }

    test("on category tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnCategoryTap(category = ContentType.MOVIE))
            awaitItem() shouldBe CatalogEvent.NavigateToCategory(category = ContentType.MOVIE)
        }
    }

    test("on search tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnSearchTap)
            awaitItem() shouldBe CatalogEvent.NavigateToSearch
        }
    }

    test("on settings tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnSettingsTap)
            awaitItem() shouldBe CatalogEvent.NavigateToSettings
        }
    }

    test("on how to tap") {
        viewModel = CatalogViewModel(
            syncCatalogUseCase = syncCatalogUseCase,
            cleanCatalogUseCase = cleanCatalogUseCase,
            database = database,
            tokenDataStore = tokenDataStore,
            userDataStore = userDataStore,
            snackbarDataStore = snackbarDataStore,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(CatalogIntent.OnHowToTap)
            awaitItem() shouldBe CatalogEvent.NavigateToHowTo
        }
    }

    test("on dismiss snackbar and snackbar action tap") {
        tokenFlow.value = ""

        viewModel = CatalogViewModel(
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

            viewModel.handleIntent(CatalogIntent.OnDismissSnackbar)
            val stateWithoutSnackbar = awaitItem()
            stateWithoutSnackbar.snackbarState shouldBe null

            cancelAndConsumeRemainingEvents()
        }

        tokenFlow.value = "token"

        val viewModelTutorial = CatalogViewModel(
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
                viewModelTutorial.handleIntent(CatalogIntent.OnSnackbarActionTap)
                awaitItem() shouldBe CatalogEvent.NavigateToHowTo
            }

            cancelAndConsumeRemainingEvents()
        }
    }

})