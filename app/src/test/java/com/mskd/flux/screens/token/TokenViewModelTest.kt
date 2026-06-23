package com.mskd.flux.screens.token

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.token.TokenRepository
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.AppInfo
import com.mskd.flux.model.tmdb.TMDBAuthentication
import com.mskd.flux.screen.token.TokenEvent
import com.mskd.flux.screen.token.TokenIntent
import com.mskd.flux.screen.token.TokenMessage
import com.mskd.flux.screen.token.TokenViewModel
import com.mskd.flux.useCases.catalog.CatalogUC
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class TokenViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: TokenViewModel
    lateinit var tokenRepository: TokenRepository
    lateinit var tmdbService: TMDBService
    lateinit var catalogUC: CatalogUC
    lateinit var appInfo: AppInfo

    beforeTest {

        tokenRepository = mockk(relaxed = true) {
            coEvery { getToken() } returns "token"
        }

        tmdbService = mockk(relaxed = true) {
            coEvery { authenticate() } returns TMDBAuthentication(success = true, code = 0, message = "")
        }

        catalogUC = mockk(relaxed = true)

        appInfo = mockk(relaxed = true)

        viewModel = TokenViewModel(
            fromSettings = true,
            tokenRepository = tokenRepository,
            tmdbService = tmdbService,
            catalogUC = catalogUC,
            appInfo = appInfo
        )

    }

    test("initial state") {
        viewModel.uiState.test {

            val initialState = awaitItem()

            initialState.token shouldBe "token"
            initialState.showBackButton shouldBe true
            initialState.message shouldBe TokenMessage.None
            initialState.isLoading shouldBe false

        }
    }

    test("set token") {
        viewModel.uiState.test {

            awaitItem()

            viewModel.handleIntent(TokenIntent.SetToken("new token"))

            val state = awaitItem()
            state.token shouldBe "new token"
            state.message shouldBe TokenMessage.None
        }
    }

    test("cancel token") {
        viewModel.event.test {

            viewModel.handleIntent(TokenIntent.OnCancelTap)

            awaitItem() shouldBe TokenEvent.NavigateToHomeScreen
            coVerify { tokenRepository.dontRequestToken() }
        }
    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(TokenIntent.OnBackTap)
            awaitItem() shouldBe TokenEvent.BackToPreviousScreen
        }
    }

    test("on next tap") {
        viewModel.event.test {
            viewModel.handleIntent(TokenIntent.OnNextTap)
            awaitItem() shouldBe TokenEvent.NavigateToHomeScreen
        }
    }

    test("initial state when fromSettings is false") {
        val vm = TokenViewModel(
            fromSettings = false,
            tokenRepository = tokenRepository,
            tmdbService = tmdbService,
            catalogUC = catalogUC,
            appInfo = appInfo
        )
        vm.uiState.test {
            val initialState = awaitItem()
            initialState.showBackButton shouldBe false
        }
    }

    test("save token when fromSettings is false success") {
        val vm = TokenViewModel(
            fromSettings = false,
            tokenRepository = tokenRepository,
            tmdbService = tmdbService,
            catalogUC = catalogUC,
            appInfo = appInfo
        )
        vm.event.test {
            vm.handleIntent(TokenIntent.SaveToken)
            awaitItem() shouldBe TokenEvent.NavigateToHomeScreen
        }
    }

    context("save token") {
        withData(
            nameFn = { it.description },
            TokenTestCases.SaveToken(
                description = "Success",
                apiResult = TMDBAuthentication(success = true, code = 0, message = ""),
                expectedMessage = TokenMessage.Success,
                expectedLoadCatalog = true,
            ),
            TokenTestCases.SaveToken(
                description = "Fail token",
                apiResult = TMDBAuthentication(success = false, code = 401, message = "Fail"),
                expectedMessage = TokenMessage.Error,
                expectedLoadCatalog = false,
            ),
            TokenTestCases.SaveToken(
                description = "Fail token with exception",
                apiResult = Exception("Fail"),
                expectedMessage = TokenMessage.Error,
                expectedLoadCatalog = false,
            )
        ) { testCase ->

            tmdbService = mockk(relaxed = true) {
                if (testCase.apiResult is TMDBAuthentication)
                    coEvery { authenticate() } returns testCase.apiResult
                else
                    coEvery { authenticate() } throws testCase.apiResult as Exception
            }

            viewModel = TokenViewModel(
                fromSettings = true,
                tokenRepository = tokenRepository,
                tmdbService = tmdbService,
                catalogUC = catalogUC,
                appInfo = appInfo
            )

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(TokenIntent.SaveToken)

                val state = awaitItem()

                if (testCase.expectedLoadCatalog) {
                    coVerify { catalogUC.syncCatalog(onlyNew = false) }
                }
                state.message shouldBe testCase.expectedMessage
                state.isLoading shouldBe false

            }

        }
    }

})