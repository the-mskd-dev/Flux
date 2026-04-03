package com.mskd.flux.screens.welcome

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.tmdb.token.TokenProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class WelcomeVIewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: WelcomeViewModel
    lateinit var tokenProvider: TokenProvider

    beforeTest {

        tokenProvider = mockk(relaxed = true)
        viewModel = WelcomeViewModel(tokenProvider = tokenProvider)

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

    test("on next tap") {
        viewModel.event.test {

            viewModel.handleIntent(WelcomeIntent.OnNextTap)

            val state = awaitItem()

            state shouldBe WelcomeEvent.ScrollToPage(1)

        }
    }

    test("on permission tap") {
        viewModel.event.test {

            viewModel.handleIntent(WelcomeIntent.OnPermissionTap)

            val state = awaitItem()

            state shouldBe WelcomeEvent.OpenPermissionDialog

        }
    }

    context("on permission granted") {
        withData(
            nameFn = { it.description },
            WelcomeTestCases.OnPermissionGranted(
                description = "has token",
                hasToken = true,
                expectedEvent = WelcomeEvent.NavigateToLibrary
            ),
            WelcomeTestCases.OnPermissionGranted(
                description = "no token",
                hasToken = false,
                expectedEvent = WelcomeEvent.NavigateToToken
            )
        ) { testCase ->

            tokenProvider = mockk(relaxed = true) {
                every { hasToken } returns testCase.hasToken
            }

            viewModel = WelcomeViewModel(tokenProvider = tokenProvider)

            viewModel.event.test {

                viewModel.handleIntent(WelcomeIntent.OnPermissionGranted)

                val state = awaitItem()

                state shouldBe testCase.expectedEvent

            }

        }
    }


})