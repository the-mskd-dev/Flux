package com.mskd.flux.features.customization.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.customization.domain.model.CustomizationDialog
import com.mskd.flux.features.customization.domain.model.NavigationStyle
import com.mskd.flux.utils.UiCommon
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.orNull
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

        // Given
        val expectedState = CustomizationDataStore.State()

        // When
        viewModel.uiState.test {
            val initialState = awaitItem()

            // Then
            initialState.uiTheme shouldBe expectedState.uiTheme
            initialState.color shouldBe expectedState.color
            initialState.waveProgress shouldBe expectedState.waveProgress
            initialState.oldBlurredHeader shouldBe expectedState.oldBlurredHeader
            initialState.largeEpisodeImage shouldBe expectedState.largeEpisodeImage
            initialState.itemsPerRow shouldBe expectedState.itemsPerRow
            initialState.itemsCorners shouldBe expectedState.itemsCorners
            initialState.seasonsPerRow shouldBe expectedState.seasonsPerRow
            initialState.navigationStyle shouldBe expectedState.navigationStyle
            initialState.dialog shouldBe null
        }

    }

    test("UiState should react to DataStoreChange") {

        checkAll(
            iterations = 100,
            Arb.enum<UiCommon.THEME>(),
            Arb.int().orNull(),
            Arb.boolean(),
            Arb.boolean(),
            Arb.boolean(),
            Arb.int(),
            Arb.int(),
            Arb.int(),
            Arb.enum<NavigationStyle>()
        ) {
            theme,
            color,
            waveProgress,
            oldBlurredHeader,
            largeEpisodeImage,
            itemsPerRow,
            itemsCorners,
            seasonsPerRow,
            navigationStyle ->

            // Given
            dataStoreFlow.value = CustomizationDataStore.State(
                uiTheme = theme,
                color = color,
                waveProgress = waveProgress,
                oldBlurredHeader = oldBlurredHeader,
                largeEpisodeImage = largeEpisodeImage,
                itemsPerRow = itemsPerRow,
                itemsCorners = itemsCorners,
                seasonsPerRow = seasonsPerRow,
                navigationStyle = navigationStyle
            )

            // When
            viewModel.uiState.test {
                val state = awaitItem()

                // Then
                state.uiTheme shouldBe theme
                state.color shouldBe color
                state.waveProgress shouldBe waveProgress
                state.oldBlurredHeader shouldBe oldBlurredHeader
                state.largeEpisodeImage shouldBe largeEpisodeImage
                state.itemsPerRow shouldBe itemsPerRow
                state.itemsCorners shouldBe itemsCorners
                state.seasonsPerRow shouldBe seasonsPerRow
                state.navigationStyle shouldBe navigationStyle

                cancelAndConsumeRemainingEvents()
            }

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

    test("ShowThemeDialog - should create a SelectDialog with THEME options") {

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

    test("ShowColorDialog - should create a SelectDialog with Int? options") {

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

    test("ShowNavigationStyleDialog - should create a SelectDialog with NavigationStyle options") {

        // Given
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(CustomizationIntent.ShowNavigationStyleDialog)

            // Then
            val dialog = awaitItem().dialog
            dialog shouldNotBe null
            dialog.shouldBeInstanceOf<CustomizationDialog.SelectDialog>()

            val selectState = dialog.state
            selectState.shouldBeInstanceOf<FluxOptionsDialogState<NavigationStyle, CustomizationIntent>>()
        }

    }

    test("SetThemeValue - should call datastore setUiTheme") {

        checkAll(
            Arb.enum<UiCommon.THEME>()
        ) { theme ->

            // Given
            viewModel.uiState.test {

                // When
                viewModel.handleIntent(CustomizationIntent.SetThemeValue(theme))

                // Then
                coVerify { customizationDataStore.setUiTheme(theme) }

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

                // When
                viewModel.handleIntent(CustomizationIntent.SetColorValue(color))

                // Then
                coVerify { customizationDataStore.setColor(color) }

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

    test("OnLargeEpisodeImageCheck - should call datastore setLargeEpisodeImage") {

        checkAll(
            Arb.boolean()
        ) { check ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.OnLargeEpisodeImageCheck(check))

                // Then
                coVerify { customizationDataStore.setLargeEpisodeImage(check) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("SetItemsPerRowValue - should call datastore setItemsPerRow") {

        checkAll(
            Arb.int(min = 2, max = 5)
        ) { count ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.SetItemsPerRowValue(count))

                // Then
                coVerify { customizationDataStore.setItemsPerRow(count) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("SetSeasonsPerRowValue - should call datastore setSeasonsPerRow") {

        checkAll(
            Arb.int(min = 2, max = 5)
        ) { count ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.SetSeasonsPerRowValue(count))

                // Then
                coVerify { customizationDataStore.setSeasonsPerRow(count) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("SetItemsCornersValue - should call datastore setItemsCorners") {

        checkAll(
            Arb.element(listOf(8, 12, 16))
        ) { count ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.SetItemsCornersValue(count))

                // Then
                coVerify { customizationDataStore.setItemsCorners(count) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("SetNavigationStyle - should call datastore setNavigationStyle") {

        checkAll(
            Arb.enum<NavigationStyle>()
        ) { style ->

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CustomizationIntent.SetNavigationStyle(style))

                // Then
                coVerify { customizationDataStore.setNavigationStyle(style) }

                cancelAndConsumeRemainingEvents()
            }

        }

    }

    test("HideDialog - should set dialog to null") {

        // Given
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(CustomizationIntent.ShowThemeDialog)
            awaitItem().dialog shouldNotBe null

            // When
            viewModel.handleIntent(CustomizationIntent.HideDialog)

            // Then
            awaitItem().dialog shouldBe null
        }

    }

})