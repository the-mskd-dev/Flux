package com.mskd.flux.features.customization.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.customization.domain.model.CustomizationDialog
import com.mskd.flux.utils.UiCommon
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalCoroutinesApi::class)
class CustomizationViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: CustomizationViewModel
    lateinit var customizationDataStore: CustomizationDataStore

    val dataStoreFlow = MutableStateFlow(CustomizationDataStore.State())

    beforeTest {

        customizationDataStore = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        viewModel = CustomizationViewModel(
            customizationDataStore = customizationDataStore,
        )

    }

    test("Initial state") {

        // Given & When
        viewModel.uiState.test {
            val initialState = awaitItem()

            // Then
            initialState.uiTheme shouldBe UiCommon.THEME.SYSTEM
            initialState.color shouldBe null
            initialState.waveProgress shouldBe true
            initialState.largeEpisodeImage shouldBe false
            initialState.itemsPerRow shouldBe 3
            initialState.dialog shouldBe null
        }

    }

    test("OnBackTap - should send BackToPreviousScreen event") {

        // Given & When
        viewModel.event.test {
            viewModel.handleIntent(CustomizationIntent.OnBackTap)

            // Then
            awaitItem() shouldBe CustomizationEvent.BackToPreviousScreen
        }

    }

    test("ShowThemeDialog - should create a SelectDialog with THEME option") {

        // Given
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(CustomizationIntent.ShowThemeDialog)

            // Then
            val dialog = awaitItem().dialog
            dialog shouldNotBe null
            dialog.shouldBeInstanceOf<CustomizationDialog.SelectDialog>()

            val selectState = dialog.state
            selectState.shouldBeInstanceOf<FluxOptionsDialogState<UiCommon.THEME, CustomizationIntent>>()
        }

    }

    test("ShowColorDialog - should create a SelectDialog with Int? option") {

        // Given
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(CustomizationIntent.ShowColorDialog)

            // Then
            val dialog = awaitItem().dialog
            dialog shouldNotBe null
            dialog.shouldBeInstanceOf<CustomizationDialog.SelectDialog>()

            val selectState = dialog.state
            selectState.shouldBeInstanceOf<FluxOptionsDialogState<Int?, CustomizationIntent>>()
            selectState.currentValue shouldBe null
        }

    }

    test("ShowItemsPerRowDialog - should show ItemsPerRowDialog") {

        // Given
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(CustomizationIntent.ShowItemsPerRowDialog)

            // Then
            val state = awaitItem()
            state.dialog.shouldBeInstanceOf<CustomizationDialog.ItemsPerRowDialog>()
        }

    }

    test("SetThemeValue - should call datastore setUiTheme") {

        checkAll(
            Arb.enum<UiCommon.THEME>()
        ) { theme ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.SetThemeValue(theme))

                // Then
                val state = awaitItem()
                coVerify { customizationDataStore.setUiTheme(theme) }
                state.dialog shouldBe null

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("SetColorValue - should call datastore setColor") {

        checkAll(
            iterations = 10,
            Arb.int()
        ) { color ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.SetColorValue(color))

                // Then
                val state = awaitItem()
                coVerify { customizationDataStore.setColor(color) }
                state.dialog shouldBe null

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("OnWaveProgressCheck - should call datastore setWaveProgress") {

        checkAll(
            Arb.boolean()
        ) { check ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.OnWaveProgressCheck(check))

                // Then
                coVerify { customizationDataStore.setWaveProgress(check) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("OnOldBlurredHeaderCheck - should call datastore setOldBlurredHeader") {

        checkAll(
            Arb.boolean()
        ) { check ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.OnOldBlurredHeaderCheck(check))

                // Then
                coVerify { customizationDataStore.setOldBlurredHeader(check) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("set large episode image check") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.OnLargeEpisodeImageCheck(true))
            dataStoreFlow.value = dataStoreFlow.value.copy(largeEpisodeImage = true)

            val state = awaitItem()

            coVerify { customizationDataStore.setLargeEpisodeImage(true) }
            state.largeEpisodeImage shouldBe true

            cancelAndConsumeRemainingEvents()
        }
    }

    test("set items per row value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.SetItemsPerRowValue(4))
            dataStoreFlow.value = dataStoreFlow.value.copy(itemsPerRow = 4)

            val state = awaitItem()

            coVerify { customizationDataStore.setItemsPerRow(4) }
            state.itemsPerRow shouldBe 4
            state.dialog shouldBe null

            cancelAndConsumeRemainingEvents()
        }
    }

    test("set seasons per row value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.SetSeasonsPerRowValue(4))
            dataStoreFlow.value = dataStoreFlow.value.copy(seasonsPerRow = 4)

            val state = awaitItem()

            coVerify { customizationDataStore.setSeasonsPerRow(4) }
            state.seasonsPerRow shouldBe 4
            state.dialog shouldBe null

            cancelAndConsumeRemainingEvents()
        }
    }

    test("set corners value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.SetItemsCornersValue(4))
            dataStoreFlow.value = dataStoreFlow.value.copy(itemsCorners = 4)

            val state = awaitItem()

            coVerify { customizationDataStore.setItemsCorners(4) }
            state.itemsCorners shouldBe 4
            state.dialog shouldBe null

            cancelAndConsumeRemainingEvents()
        }
    }

    test("hide dialog") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(CustomizationIntent.ShowThemeDialog)
            awaitItem().dialog shouldNotBe null

            viewModel.handleIntent(CustomizationIntent.HideDialog)
            awaitItem().dialog shouldBe null
        }
    }

})