package com.mskd.flux.features.setup.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.setup.domain.model.SetupScreen
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk

class SetupViewModelTest : FunSpec( {

    fluxExtensions()

    lateinit var settingsDataStore: SettingsDataStore
    lateinit var viewModel: SetupViewModel

    beforeTest {

        settingsDataStore = mockk(relaxed = true)

        viewModel = SetupViewModel(settingsDataStore = settingsDataStore)

    }

    test("onNextButton - WELCOME - navigate to SOURCES") {
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

    test("onNextButton - SOURCES - if mode is DEFAULT, sent permissions event") {
        // Given : go to SOURCES screen
        viewModel.handleIntent(SetupIntent.OnNextButton)

        viewModel.event.test {

            // When
            viewModel.handleIntent(SetupIntent.OnNextButton)

            // Then
            awaitItem() shouldBe SetupEvent.ShowPermissionDialog

        }
    }

    test("onNextButton - SOURCES - if system folders are disabled, navigate to Token") {
        // Given : go to SOURCES screen, then change mode to CUSTOM
        viewModel.handleIntent(SetupIntent.OnNextButton)
        viewModel.handleIntent(SetupIntent.EnableSystemFolders(enabled = false))

        viewModel.event.test {

            // When
            viewModel.handleIntent(SetupIntent.OnNextButton)

            // Then
            awaitItem() shouldBe SetupEvent.NavigateToToken

        }
    }

    test("enableSystemFolders - change mode in SettingsDatastore") {
        viewModel.uiState.test {

            // Given
            awaitItem()

            // When
            viewModel.handleIntent(SetupIntent.EnableSystemFolders(enabled = false))

            // Then
            awaitItem().systemFoldersEnabled shouldBe false

            coVerify(exactly = 1) {
                settingsDataStore.setSystemFolders(enabled = false)
            }

        }
    }

    test("onPermissionGranted - navigate to TokenScreen") {
        viewModel.event.test {

            // When
            viewModel.handleIntent(SetupIntent.OnPermissionGranted)

            // Then
            awaitItem() shouldBe SetupEvent.NavigateToToken

        }
    }

})