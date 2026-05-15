package com.mskd.flux.screens.settings

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.mockups.mockkCatalogUC
import com.mskd.flux.mockups.mockkImagesUC
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.images.ImagesUC
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

class SettingsViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: SettingsViewModel
    lateinit var settingsRepository: SettingsRepository
    lateinit var catalogUC: CatalogUC
    lateinit var imagesUC: ImagesUC

    val dataStoreFlow = MutableStateFlow(SettingsRepository.State())

    beforeTest {

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        catalogUC = mockkCatalogUC()

        imagesUC = mockkImagesUC()

        viewModel = SettingsViewModel(
            settingsRepository = settingsRepository,
            catalogUC = catalogUC,
            imagesUC = imagesUC
        )

    }

    test("initial state") {
        viewModel.uiState.test {
            val initialState = awaitItem()
            initialState.rewindValue shouldBe 10
            initialState.forwardValue shouldBe 10
            initialState.uiTheme shouldBe Ui.THEME.SYSTEM
            initialState.dialogState shouldBe null
        }
    }

    test("show rewind dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowRewindDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Int>>().should {
                it.currentValue.shouldBeInstanceOf<Int>()
            }

        }
    }

    test("show forward dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowForwardDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Int>>().should {
                it.currentValue.shouldBeInstanceOf<Int>()
            }

        }
    }

    test("show ui theme dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowThemeDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Ui.THEME>>().should {
                it.currentValue.shouldBeInstanceOf<Ui.THEME>()
            }
        }
    }

    test("set rewind value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetRewindValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerRewindValue = 20)

            val state = awaitItem()

            coVerify { settingsRepository.setPlayerRewindValue(20) }
            state.rewindValue shouldBe 20
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set_forward_value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetForwardValue(20))
            dataStoreFlow.value = dataStoreFlow.value.copy(playerForwardValue = 20)

            val state = awaitItem()

            coVerify { settingsRepository.setPlayerForwardValue(20) }
            state.forwardValue shouldBe 20
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set ui theme") {

        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetThemeValue(Ui.THEME.DARK))
            dataStoreFlow.value = dataStoreFlow.value.copy(uiTheme = Ui.THEME.DARK)

            val state = awaitItem()

            coVerify { settingsRepository.setUiTheme(Ui.THEME.DARK) }
            state.uiTheme shouldBe Ui.THEME.DARK
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }

    }

    test("show data language dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowLanguageDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            state.dialogState.shouldBeInstanceOf<SettingsDialogState<Locale?>>()
        }
    }

    test("set data language value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetLanguageValue(Locale.FRENCH))
            dataStoreFlow.value = dataStoreFlow.value.copy(dataLanguage = Locale.FRENCH)

            val state = awaitItem()

            coVerify { settingsRepository.setDataLanguage(Locale.FRENCH) }
            state.languageValue shouldBe Locale.FRENCH
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set system data language value") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.SetLanguageValue(null))
            dataStoreFlow.value = dataStoreFlow.value.copy(dataLanguage = null)

            val state = awaitItem()

            coVerify { settingsRepository.setDataLanguage(null) }
            state.languageValue shouldBe null
            state.dialogState shouldBe null

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set auto keyboard") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.OnAutoKeyboardCheck(false))
            dataStoreFlow.value = dataStoreFlow.value.copy(autoKeyboard = false)

            val state = awaitItem()

            coVerify { settingsRepository.setAutoKeyboard(false) }
            state.autoKeyboard shouldBe false

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set external player") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.OnExternalPlayerCheck(true))
            dataStoreFlow.value = dataStoreFlow.value.copy(externalPlayer = true)

            val state = awaitItem()

            coVerify { settingsRepository.setExternalPlayer(true) }
            state.useExternalPlayer shouldBe true

            cancelAndConsumeRemainingEvents()

        }
    }

    test("set prefetch images") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.OnPrefetchImagesCheck(true))
            dataStoreFlow.value = dataStoreFlow.value.copy(prefetchImages = true)

            val state = awaitItem()

            coVerify { settingsRepository.setPrefetchImages(true) }
            state.prefetchImages shouldBe true

            cancelAndConsumeRemainingEvents()

        }
    }

})