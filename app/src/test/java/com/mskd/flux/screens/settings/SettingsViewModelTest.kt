package com.mskd.flux.screens.settings

import android.app.Application
import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.mockups.mockkCatalogUC
import com.mskd.flux.mockups.mockkImagesUC
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.images.ImagesUC
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest : FunSpec({

    fluxExtensions()

    lateinit var viewModel: SettingsViewModel
    lateinit var settingsRepository: SettingsRepository
    lateinit var catalogUC: CatalogUC
    lateinit var imagesUC: ImagesUC
    lateinit var application: Application

    val dataStoreFlow = MutableStateFlow(SettingsRepository.State())

    beforeTest {

        settingsRepository = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        catalogUC = mockkCatalogUC()

        imagesUC = mockkImagesUC()

        application = mockk(relaxed = true) {
            every { getString(any()) } returns "System"
        }

        viewModel = SettingsViewModel(
            application = application,
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
            initialState.dialogState shouldBe null
            initialState.showSyncDialog shouldBe false
            initialState.fullSyncInProgress shouldBe false
            initialState.prefetchImages shouldBe false
        }
    }

    test("on back tap") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnBackTap)
            awaitItem() shouldBe SettingsEvent.BackToPreviousScreen
        }
    }

    test("on token tap") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnTokenTap)
            awaitItem() shouldBe SettingsEvent.NavigateToTokenScreen
        }
    }

    test("on about tap") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnAboutTap)
            awaitItem() shouldBe SettingsEvent.NavigateToAboutScreen
        }
    }

    test("on how to tap") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnHowToTap)
            awaitItem() shouldBe SettingsEvent.NavigateToHowToScreen
        }
    }

    test("on customization tap") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnCustomizationTap)
            awaitItem() shouldBe SettingsEvent.NavigateToCustomizationScreen
        }
    }

    test("show full sync dialog") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowFullSyncDialog(true))
            awaitItem().showSyncDialog shouldBe true

            viewModel.handleIntent(SettingsIntent.ShowFullSyncDialog(false))
            awaitItem().showSyncDialog shouldBe false
        }
    }

    test("proceed full sync") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowFullSyncDialog(true))
            awaitItem().showSyncDialog shouldBe true

            viewModel.handleIntent(SettingsIntent.ProceedFullSync)
            awaitItem().showSyncDialog shouldBe false

            coVerify { catalogUC.syncCatalog(onlyNew = false) }
        }
    }

    test("hide dialog") {
        viewModel.uiState.test {
            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowRewindDialog)
            awaitItem().dialogState shouldNotBe null

            viewModel.handleIntent(SettingsIntent.HideDialog)
            awaitItem().dialogState shouldBe null
        }
    }

    test("show rewind dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowRewindDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            val dialogState = state.dialogState
            dialogState.shouldBeInstanceOf<FluxOptionsDialogState<Int, SettingsIntent>>()
            dialogState.currentValue shouldBe 10

        }
    }

    test("show forward dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowForwardDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            val dialogState = state.dialogState
            dialogState.shouldBeInstanceOf<FluxOptionsDialogState<Int, SettingsIntent>>()
            dialogState.currentValue shouldBe 10

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

    test("show data language dialog") {
        viewModel.uiState.test {

            awaitItem()
            viewModel.handleIntent(SettingsIntent.ShowLanguageDialog)

            val state = awaitItem()
            state.dialogState shouldNotBe null
            val dialogState = state.dialogState
            dialogState.shouldBeInstanceOf<FluxOptionsDialogState<Locale?, SettingsIntent>>()
            dialogState.currentValue shouldBe null
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

    test("set external player - request permission when checked is true") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnExternalPlayerCheck(true))
            awaitItem() shouldBe SettingsEvent.RequestExternalPlayerPermission
        }
    }

    test("set external player - does not request permission when checked is false") {
        viewModel.event.test {
            viewModel.handleIntent(SettingsIntent.OnExternalPlayerCheck(false))
            expectNoEvents()
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

    test("set prefetch images - triggers prefetch when checked is true") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.OnPrefetchImagesCheck(true))
            coVerify { imagesUC.prefetchImages() }
        }
    }

    test("set prefetch images - does not trigger prefetch when checked is false") {
        viewModel.uiState.test {
            awaitItem()

            viewModel.handleIntent(SettingsIntent.OnPrefetchImagesCheck(false))
            coVerify(exactly = 0) { imagesUC.prefetchImages() }
        }
    }

})