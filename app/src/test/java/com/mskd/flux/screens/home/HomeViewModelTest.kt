package com.mskd.flux.screens.home

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.snackbars.SnackbarRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.tmdb.token.TokenRepository
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.mockkCatalogUC
import com.mskd.flux.mockups.mockkSnackbarRepository
import com.mskd.flux.model.ScreenState
import com.mskd.flux.useCases.catalog.CatalogUC
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
                snackbarRepository = snackbarRepository
            )

            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.screenState shouldBe ScreenState.CONTENT
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
            snackbarRepository = snackbarRepository
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
            snackbarRepository = snackbarRepository
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
            snackbarRepository = snackbarRepository
        )

        coVerify(exactly = 0) {
            catalogUC.syncCatalog(any())
        }
    }

})