package com.mskd.flux.features.welcome.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class WelcomeViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: WelcomeViewModel

    beforeTest {

        viewModel = WelcomeViewModel()

    }

    test("initial state") {
        viewModel.uiState.test {

            val state = awaitItem()

            state.pageIndex shouldBe 0
            state.buttons shouldBe listOf(WelcomeButton.NEXT)

        }
    }

    test("on page change") {
        viewModel.uiState.test {

            awaitItem()

            // Test to change to permissions page
            viewModel.handleIntent(WelcomeIntent.OnPageChange(WelcomePage.PERMISSIONS.ordinal))

            var state = awaitItem()

            state.pageIndex shouldBe WelcomePage.PERMISSIONS.ordinal
            state.buttons shouldBe listOf(WelcomeButton.PREVIOUS, WelcomeButton.PERMISSIONS)

            // Test to change to welcome page
            viewModel.handleIntent(WelcomeIntent.OnPageChange(WelcomePage.WELCOME.ordinal))

            state = awaitItem()

            state.pageIndex shouldBe WelcomePage.WELCOME.ordinal
            state.buttons shouldBe listOf(WelcomeButton.NEXT)

        }
    }


    test("on previous tap") {
        viewModel.uiState.test {

            viewModel.handleIntent(WelcomeIntent.OnPageChange(pageIndex = WelcomePage.lastIndex))
            awaitItem()

            viewModel.event.test {

                viewModel.handleIntent(WelcomeIntent.OnPreviousTap)

                val state = awaitItem()

                state shouldBe WelcomeEvent.ScrollToPage(WelcomePage.lastIndex - 1)

            }

            cancelAndConsumeRemainingEvents()

        }
    }

    test("on previous tap when at first page") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.event.test {
                viewModel.handleIntent(WelcomeIntent.OnPreviousTap)
                val state = awaitItem()
                state shouldBe WelcomeEvent.ScrollToPage(0)
            }

            cancelAndConsumeRemainingEvents()
        }
    }

    test("on next tap") {
        viewModel.event.test {

            viewModel.handleIntent(WelcomeIntent.OnNextTap)

            val state = awaitItem()

            state shouldBe WelcomeEvent.ScrollToPage(1)

        }
    }

    test("on next tap when at last page") {
        viewModel.uiState.test {
            viewModel.handleIntent(WelcomeIntent.OnPageChange(pageIndex = WelcomePage.lastIndex))
            awaitItem()

            viewModel.event.test {
                viewModel.handleIntent(WelcomeIntent.OnNextTap)
                val state = awaitItem()
                state shouldBe WelcomeEvent.ScrollToPage(WelcomePage.lastIndex)
            }

            cancelAndConsumeRemainingEvents()
        }
    }

    test("on permission tap, should navigate open permission dialog") {
        viewModel.event.test {

            viewModel.handleIntent(WelcomeIntent.OnPermissionTap)

            val state = awaitItem()

            state shouldBe WelcomeEvent.OpenPermissionDialog

        }
    }

    test("on permission granted, should navigate to token") {

        viewModel.event.test {

            viewModel.handleIntent(WelcomeIntent.OnPermissionGranted)

            val state = awaitItem()

            state shouldBe WelcomeEvent.NavigateToToken

        }
    }


})