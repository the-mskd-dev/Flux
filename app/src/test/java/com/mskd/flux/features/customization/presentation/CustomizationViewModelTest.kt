package com.mskd.flux.features.customization.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.utils.UiCommon
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

    test("initial state") {
        viewModel.uiState.test {
            val initialState = awaitItem()
            initialState.uiTheme shouldBe UiCommon.THEME.SYSTEM
            initialState.color shouldBe null
            initialState.waveProgress shouldBe true
            initialState.largeEpisodeImage shouldBe false
            initialState.itemsPerRow shouldBe 3
            initialState.dialog shouldBe null
        }
    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(CustomizationIntent.OnBackTap)
            awaitItem() shouldBe CustomizationEvent.BackToPreviousScreen
        }
    }

    test("show theme dialog") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(CustomizationIntent.ShowThemeDialog)

            val state = awaitItem()
            state.dialog shouldNotBe null
            val dialog = state.dialog
            dialog.shouldBeInstanceOf<CustomizationDialog.SelectDialog>()
            val selectState = dialog.state
            selectState.shouldBeInstanceOf<FluxOptionsDialogState<UiCommon.THEME, CustomizationIntent>>()
            selectState.currentValue shouldBe UiCommon.THEME.SYSTEM
        }
    }

    test("show color dialog") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(CustomizationIntent.ShowColorDialog)

            val state = awaitItem()
            state.dialog shouldNotBe null
            val dialog = state.dialog
            dialog.shouldBeInstanceOf<CustomizationDialog.SelectDialog>()
            val selectState = dialog.state
            selectState.shouldBeInstanceOf<FluxOptionsDialogState<Int?, CustomizationIntent>>()
            selectState.currentValue shouldBe null
        }
    }

    test("show items per row dialog") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(CustomizationIntent.ShowItemsPerRowDialog)

            val state = awaitItem()
            state.dialog.shouldBeInstanceOf<CustomizationDialog.ItemsPerRowDialog>()
        }
    }

    test("set theme value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.SetThemeValue(UiCommon.THEME.DARK))
            dataStoreFlow.value = dataStoreFlow.value.copy(uiTheme = UiCommon.THEME.DARK)

            val state = awaitItem()

            coVerify { customizationDataStore.setUiTheme(UiCommon.THEME.DARK) }
            state.uiTheme shouldBe UiCommon.THEME.DARK
            state.dialog shouldBe null

            cancelAndConsumeRemainingEvents()
        }
    }

    test("set color value") {
        viewModel.uiState.test {
            awaitItem()

            val testColor = 0xFF00FF00.toInt()
            viewModel.handleIntent(CustomizationIntent.SetColorValue(testColor))
            dataStoreFlow.value = dataStoreFlow.value.copy(color = testColor)

            val state = awaitItem()

            coVerify { customizationDataStore.setColor(testColor) }
            state.color shouldBe testColor
            state.dialog shouldBe null

            cancelAndConsumeRemainingEvents()
        }
    }

    test("set wave progress check") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.OnWaveProgressCheck(false))
            dataStoreFlow.value = dataStoreFlow.value.copy(waveProgress = false)

            val state = awaitItem()

            coVerify { customizationDataStore.setWaveProgress(false) }
            state.waveProgress shouldBe false

            cancelAndConsumeRemainingEvents()
        }
    }

    test("set old blurred header check") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(CustomizationIntent.OnOldBlurredHeaderCheck(true))
            dataStoreFlow.value = dataStoreFlow.value.copy(oldBlurredHeader = true)

            val state = awaitItem()

            coVerify { customizationDataStore.setOldBlurredHeader(true) }
            state.oldBlurredHeader shouldBe true

            cancelAndConsumeRemainingEvents()
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