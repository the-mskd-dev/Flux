package com.mskd.flux.screens.token

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.catalog.CatalogRepository
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.data.tmdb.token.TokenProvider
import com.mskd.flux.model.tmdb.TMDBAuthentication
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class TokenViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: TokenViewModel
    lateinit var tokenProvider: TokenProvider
    lateinit var tmdbService: TMDBService
    lateinit var catalogRepository: CatalogRepository

    beforeTest {

        tokenProvider = mockk(relaxed = true) {
            coEvery { getToken() } returns "token"
        }

        tmdbService = mockk(relaxed = true) {
            coEvery { authenticate() } returns TMDBAuthentication(success = true, status_code = 0, status_message = "")
        }

        catalogRepository = mockk(relaxed = true)

        viewModel = TokenViewModel(
            fromSettings = true,
            tokenProvider = tokenProvider,
            tmdbService = tmdbService,
            catalogRepository = catalogRepository
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
            coVerify { tokenProvider.dontRequestToken() }
        }
    }

    context("save token") {
        withData(
            nameFn = { it.description },
            TokenTestCases.SaveToken(
                description = "Success",
                apiResult = TMDBAuthentication(success = true, status_code = 0, status_message = ""),
                expectedMessage = TokenMessage.Success,
                expectedLoadCatalog = true,
            ),
            TokenTestCases.SaveToken(
                description = "Fail token",
                apiResult = TMDBAuthentication(success = false, status_code = 401, status_message = "Fail"),
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
                tokenProvider = tokenProvider,
                tmdbService = tmdbService,
                catalogRepository = catalogRepository
            )

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(TokenIntent.SaveToken)

                val state = awaitItem()

                if (testCase.expectedLoadCatalog) {
                    coEvery { catalogRepository.syncCatalog() }
                }
                state.message shouldBe testCase.expectedMessage
                state.isLoading shouldBe false

            }

        }
    }

})