package com.mskd.flux.features.token.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.features.token.domain.model.AuthenticateResult
import com.mskd.flux.features.token.domain.model.TokenMessage
import com.mskd.flux.features.token.domain.usecase.SaveTokenAndSyncUseCase
import com.mskd.flux.features.token.fake.TokenTestCases
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
    lateinit var saveTokenAndSyncUseCase: SaveTokenAndSyncUseCase
    lateinit var appInfo: AppInfo

    beforeTest {

        tokenDataStore = mockk<TokenDataStore>(relaxed = true) {
            coEvery { getToken() } returns "token"
        }

        saveTokenAndSyncUseCase = mockk<SaveTokenAndSyncUseCase>(relaxed = true)
        coEvery { saveTokenAndSyncUseCase(any<String>()) } returns AuthenticateResult.SUCCESS

        appInfo = mockk(relaxed = true)

        viewModel = TokenViewModel(
            fromSetup = false,
            tokenDataStore = tokenDataStore,
            saveTokenAndSyncUseCase = saveTokenAndSyncUseCase,
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

    test("initial state when fromSetup is true") {
        val vm = TokenViewModel(
            fromSetup = true,
            tokenDataStore = tokenDataStore,
            saveTokenAndSyncUseCase = saveTokenAndSyncUseCase,
            appInfo = appInfo
        )
        vm.uiState.test {
            val initialState = awaitItem()
            initialState.showBackButton shouldBe false
        }
    }

    test("save token when fromSetup is true success") {
        val vm = TokenViewModel(
            fromSetup = true,
            tokenDataStore = tokenDataStore,
            saveTokenAndSyncUseCase = saveTokenAndSyncUseCase,
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
                apiResult = AuthenticateResult.SUCCESS,
                expectedMessage = TokenMessage.Success,
                expectedLoadCatalog = true,
            ),
            TokenTestCases.SaveToken(
                description = "Fail token",
                apiResult = AuthenticateResult.FAILURE,
                expectedMessage = TokenMessage.Error,
                expectedLoadCatalog = false,
            ),
            TokenTestCases.SaveToken(
                description = "Fail token with exception",
                apiResult = AuthenticateResult.FAILURE,
                expectedMessage = TokenMessage.Error,
                expectedLoadCatalog = false,
            )
        ) { testCase ->

            saveTokenAndSyncUseCase = mockk<SaveTokenAndSyncUseCase>(relaxed = true)
            coEvery { saveTokenAndSyncUseCase(any<String>()) } returns (testCase.apiResult as AuthenticateResult)

            viewModel = TokenViewModel(
                fromSetup = false,
                tokenDataStore = tokenDataStore,
                saveTokenAndSyncUseCase = saveTokenAndSyncUseCase,
                appInfo = appInfo
            )

            viewModel.uiState.test {

                awaitItem()

                viewModel.handleIntent(TokenIntent.SaveToken)

                val state = awaitItem()

                if (testCase.expectedLoadCatalog) {
                    coVerify { saveTokenAndSyncUseCase(any()) }
                }
                state.message shouldBe testCase.expectedMessage
                state.isLoading shouldBe false

            }

        }
    }

})