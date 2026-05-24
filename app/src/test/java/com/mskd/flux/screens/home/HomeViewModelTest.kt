package com.mskd.flux.screens.home

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.snackbars.SnackbarRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.tmdb.token.TokenRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.mockkCatalogUC
import com.mskd.flux.mockups.mockkSnackbarRepository
import com.mskd.flux.model.AppInfo
import com.mskd.flux.model.ScreenState
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.model.artwork.Artwork
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
    lateinit var catalogUC: CatalogUC
    lateinit var userRepository: UserRepository
    lateinit var tokenRepository: TokenRepository
    lateinit var snackbarRepository: SnackbarRepository
    lateinit var appInfo: AppInfo

    // Mocked flows
    val dataStoreFlow = MutableStateFlow(UserRepository.State())
    val tokenFlow = MutableStateFlow("token")

    beforeTest {

        catalogUC = mockkCatalogUC()

        tokenRepository = mockk(relaxed = true) {
            coEvery { flow } returns tokenFlow
        }
        userRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        snackbarRepository = mockkSnackbarRepository()

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
                catalogUC = catalogUC,
                tokenRepository = tokenRepository,
                userRepository = userRepository,
                snackbarRepository = snackbarRepository,
                appInfo = appInfo
            )

            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.screenState shouldBe HomeUiState.State.Content
                initialState.artworks shouldBe MediaMockups.artworks
                initialState.lastWatchedMediaIds shouldBe emptyList()
                initialState.isRefreshing shouldBe false
                initialState.snackbarState shouldBe testCase.expectedSnackbarState

                cancelAndConsumeRemainingEvents()
            }

        }
    }

    test("should force sync when manual sync requested") {

        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        viewModel.handleIntent(HomeIntent.SyncCatalog)

        coVerify {
            catalogUC.syncCatalog(onlyNew = true)
        }
    }


    test("should sync when last sync was more than 1 day ago") {
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns oldTime

        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        coVerify(exactly = 1) {
            catalogUC.syncCatalog(onlyNew = true)
        }
    }

    test("should not sync when last sync was less than 1 day ago") {
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns recentTime

        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        coVerify(exactly = 0) {
            catalogUC.syncCatalog(any())
        }
    }

    test("should sync when new app version") {

        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userRepository.getSyncTime() } returns recentTime

        appInfo = AppInfo(
            versionCode = Int.MAX_VALUE,
            versionName = "VersionTest"
        )

        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        coVerify(exactly = 1) {
            catalogUC.syncCatalog(onlyNew = false)
        }
    }

    test("on normal artwork tap") {
        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnArtworkTap(artworkId = 123L, rgb = 0x112233))
            awaitItem() shouldBe HomeEvent.NavigateToArtwork(artworkId = 123L, rgb = 0x112233)
        }
    }

    test("on unknown artwork tap") {
        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnArtworkTap(artworkId = Artwork.UNKNOWN_ID, rgb = null))
            awaitItem() shouldBe HomeEvent.NavigateToUnknown
        }
    }

    test("on category tap") {
        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnCategoryTap(category = com.mskd.flux.model.artwork.ContentType.MOVIE))
            awaitItem() shouldBe HomeEvent.NavigateToCategory(category = com.mskd.flux.model.artwork.ContentType.MOVIE)
        }
    }

    test("on search tap") {
        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnSearchTap)
            awaitItem() shouldBe HomeEvent.NavigateToSearch
        }
    }

    test("on settings tap") {
        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
            appInfo = appInfo
        )

        viewModel.event.test {
            viewModel.handleIntent(HomeIntent.OnSettingsTap)
            awaitItem() shouldBe HomeEvent.NavigateToSettings
        }
    }

    test("on how to tap") {
        viewModel = HomeViewModel(
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
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
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
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
            catalogUC = catalogUC,
            tokenRepository = tokenRepository,
            userRepository = userRepository,
            snackbarRepository = snackbarRepository,
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