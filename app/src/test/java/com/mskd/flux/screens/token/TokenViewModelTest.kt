package com.mskd.flux.screens.token

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.domain.model.core.AppInfo
import com.mskd.flux.core.network.tmdb.data.remote.dto.AuthenticationDto
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.tmdb.data.service.TMDBService
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.features.token.domain.model.TokenMessage
import com.mskd.flux.features.token.presentation.TokenEvent
import com.mskd.flux.features.token.presentation.TokenIntent
import com.mskd.flux.features.token.presentation.TokenViewModel
import com.mskd.flux.mockups.features.catalog.FakeSyncCatalogUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class TokenViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: TokenViewModel
    lateinit var tokenDataStore: TokenDataStore
    lateinit var tmdbService: TMDBService
    lateinit var syncCatalogUseCase: SyncCatalogUseCase
    lateinit var appInfo: AppInfo

    beforeTest {

        tokenDataStore = mockk(relaxed = true) {
            coEvery { getToken() } returns "token"
        }

        tmdbService = mockk(relaxed = true) {
            coEvery { authenticate() } returns AuthenticationDto(success = true, code = 0, message = "")
        }

        syncCatalogUseCase = FakeSyncCatalogUseCase()

        appInfo = mockk(relaxed = true)

        viewModel = TokenViewModel(
            fromSettings = true,
            tokenDataStore = tokenDataStore,
            tmdbService = tmdbService,
            syncCatalogUseCase = syncCatalogUseCase,
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

            awaitItem() shouldBe TokenEvent.NavigateToCatalogScreen
            coVerify { tokenDataStore.dontRequestToken() }
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
            awaitItem() shouldBe TokenEvent.NavigateToCatalogScreen
        }
    }

    test("initial state when fromSettings is false") {
        val vm = TokenViewModel(
            fromSettings = false,
            tokenDataStore = tokenDataStore,
            tmdbService = tmdbService,
            syncCatalogUseCase = syncCatalogUseCase,
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
            tokenDataStore = tokenDataStore,
            tmdbService = tmdbService,
            syncCatalogUseCase = syncCatalogUseCase,
            appInfo = appInfo
        )
        vm.event.test {
            vm.handleIntent(TokenIntent.SaveToken)
            awaitItem() shouldBe TokenEvent.NavigateToCatalogScreen
        }
    }

    context("save token") {
        withData(
            nameFn = { it.description },
            TokenTestCases.SaveToken(
                description = "Success",
                apiResult = AuthenticationDto(success = true, code = 0, message = ""),
                expectedMessage = TokenMessage.Success,
                expectedLoadCatalog = true,
            ),
            TokenTestCases.SaveToken(
                description = "Fail token",
                apiResult = AuthenticationDto(success = false, code = 401, message = "Fail"),
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
                if (testCase.apiResult is AuthenticationDto)
                    coEvery { authenticate() } returns testCase.apiResult
                else
                    coEvery { authenticate() } throws testCase.apiResult as Exception
            }

            viewModel = TokenViewModel(
                fromSettings = true,
                tokenDataStore = tokenDataStore,
                tmdbService = tmdbService,
                syncCatalogUseCase = syncCatalogUseCase,
                appInfo = appInfo
            )

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(TokenIntent.SaveToken)

                val state = awaitItem()

                if (testCase.expectedLoadCatalog) {
                    coVerify { syncCatalogUseCase(onlyNew = false) }
                }
                state.message shouldBe testCase.expectedMessage
                state.isLoading shouldBe false

            }

        }
    }

})