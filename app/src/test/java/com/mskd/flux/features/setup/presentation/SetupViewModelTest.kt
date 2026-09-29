package com.mskd.flux.features.setup.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.setup.domain.model.SetupScreen
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

class SetupViewModelTest : FunSpec( {

    fluxExtensions()

    lateinit var settingsDataStore: SettingsDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var viewModel: SetupViewModel

    beforeTest {

        settingsDataStore = mockk(relaxed = true)
        tokenDataStore = mockk(relaxed = true)

        viewModel = SetupViewModel(
            settingsDataStore = settingsDataStore,
            tokenDataStore = tokenDataStore
        )

    }

    context("onNextButton") {

        test("from WELCOME should navigate to SOURCES") {
            viewModel.uiState.test {

                // Given
                awaitItem()

                // When
                viewModel.handleIntent(SetupIntent.OnNextButton)

                // Then
                val screen = awaitItem().screen
                screen shouldBe SetupScreen.SOURCES

            }
        }

        test("from SOURCES, if mode is DEFAULT, should sent permissions event") {
            // Given : go to SOURCES screen
            viewModel.handleIntent(SetupIntent.OnNextButton)

            viewModel.event.test {

                // When
                viewModel.handleIntent(SetupIntent.OnNextButton)

                // Then
                awaitItem() shouldBe SetupEvent.ShowPermissionDialog

            }
        }

        test("SOURCES, if system folders are disabled should emit NavigateToSources") {
            // Given : go to SOURCES screen, then change mode to CUSTOM
            viewModel.handleIntent(SetupIntent.OnNextButton)
            viewModel.handleIntent(SetupIntent.EnableSystemFolders(enabled = false))

            viewModel.event.test {

                // When
                viewModel.handleIntent(SetupIntent.OnNextButton)

                // Then
                awaitItem() shouldBe SetupEvent.NavigateToSources

            }
        }


    }

    test("EnableSystemFolders should change mode in SettingsDatastore") {
        viewModel.uiState.test {

            // Given
            awaitItem()

            // When
            viewModel.handleIntent(SetupIntent.EnableSystemFolders(enabled = false))

            // Then
            awaitItem().systemFoldersEnabled shouldBe false
            coVerify(exactly = 1) { settingsDataStore.setSystemFolders(enabled = false) }

        }
    }

    context("onPermissionGranted") {

        test("if token is requested should emit NavigateToToken event") {

            //Given
            every { tokenDataStore.tokenRequested } returns true

            viewModel.event.test {

                // When
                viewModel.handleIntent(SetupIntent.OnPermissionGranted)

                // Then
                val event = awaitItem()
                event shouldBe SetupEvent.NavigateToToken

            }
        }

        test("if token is not requested should emit NavigateToCatalog event") {

            //Given
            every { tokenDataStore.tokenRequested } returns false

            viewModel.event.test {

                // When
                viewModel.handleIntent(SetupIntent.OnPermissionGranted)

                // Then
                awaitItem() shouldBe SetupEvent.NavigateToCatalog

            }
        }

    }

})