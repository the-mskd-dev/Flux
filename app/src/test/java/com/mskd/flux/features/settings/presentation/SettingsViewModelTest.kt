package com.mskd.flux.features.settings.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.disablePrivateFolder.DisablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder.EnablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.setIncludeNsfw.SetIncludeNsfwUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.settings.domain.model.SettingsDialog
import com.mskd.flux.system.EmailLauncher
import com.mskd.flux.system.UrlLauncher
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.mockk.clearMocks
import io.mockk.coEvery
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
    lateinit var appInfo: AppInfo
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var imagesPrefetchManager: ImagesPrefetchManager
    lateinit var syncCatalogUseCase: SyncCatalogUseCase
    lateinit var updateLanguageUseCase: UpdateLanguageUseCase
    lateinit var enablePrivateFolderUseCase: EnablePrivateFolderUseCase
    lateinit var disablePrivateFolderUseCase: DisablePrivateFolderUseCase
    lateinit var setIncludeNsfwUseCase: SetIncludeNsfwUseCase
    lateinit var emailLauncher: EmailLauncher
    lateinit var urlLauncher: UrlLauncher

    val dataStoreFlow = MutableStateFlow(SettingsDataStore.State())
    val privateFolderFlow = MutableStateFlow(PrivateFolderDataStore.State())
    val imagesPrefetchFlow = MutableStateFlow(ImagesPrefetchManager.State.Idle)
    val syncCatalogFlow = MutableStateFlow(SyncState.Idle)

    beforeTest {

        appInfo = AppInfo(
            versionCode = 616,
            versionName = "1.6.10"
        )

        settingsDataStore = mockk(relaxed = true) {
            every { flow } returns dataStoreFlow
        }

        privateFolderDataStore = mockk(relaxed = true) {
            every { flow } returns privateFolderFlow
        }

        imagesPrefetchManager = mockk(relaxed = true) {
            every { state } returns imagesPrefetchFlow
        }

        syncCatalogUseCase = mockk(relaxed = true) {
            every { state } returns syncCatalogFlow
        }
        updateLanguageUseCase = mockk(relaxed = true)
        enablePrivateFolderUseCase = mockk(relaxed = true)
        disablePrivateFolderUseCase = mockk(relaxed = true)
        setIncludeNsfwUseCase = mockk(relaxed = true)

        emailLauncher = mockk(relaxed = true)
        urlLauncher = mockk(relaxed = true)

        viewModel = SettingsViewModel(
            appInfo = appInfo,
            settingsDataStore = settingsDataStore,
            privateFolderDataStore = privateFolderDataStore,
            imagesPrefetchManager = imagesPrefetchManager,
            syncCatalogUseCase = syncCatalogUseCase,
            updateLanguageUseCase = updateLanguageUseCase,
            enablePrivateFolderUseCase = enablePrivateFolderUseCase,
            disablePrivateFolderUseCase = disablePrivateFolderUseCase,
            setIncludeNsfwUseCase = setIncludeNsfwUseCase,
            emailLauncher = emailLauncher,
            urlLauncher = urlLauncher
        )

    }

    context("Initial State") {
        test("should return default values") {
            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.rewindValue shouldBe 10
                initialState.forwardValue shouldBe 10
                initialState.optionsDialog shouldBe null
                initialState.settingsDialog shouldBe null
                initialState.fullSyncInProgress shouldBe false
                initialState.prefetchHdImages shouldBe false
                initialState.privateFolderEnabled shouldBe false
                initialState.privateFolderIncludeNsfw shouldBe true
            }
        }

        test("should react to value changes") {

            checkAll(
                iterations = 30,
                Arb.int(),
                Arb.int(),
                Exhaustive.boolean(),
                Exhaustive.boolean(),
                Exhaustive.boolean(),
                Exhaustive.boolean(),
                Exhaustive.boolean(),
                Exhaustive.boolean(),
            ) {
                rewindValue,
                forwardValue,
                prefetchHdImages,
                privateFolderEnabled,
                privateFolderIncludeNsfw,
                pipIsEnabled,
                autoKeyboard,
                externalPlayer ->

                // Given
                dataStoreFlow.value = dataStoreFlow.value.copy(
                    playerRewindValue = rewindValue,
                    playerForwardValue = forwardValue,
                    prefetchHdImages = prefetchHdImages,
                    pipIsEnabled = pipIsEnabled,
                    autoKeyboard = autoKeyboard,
                    externalPlayer = externalPlayer
                )
                privateFolderFlow.value = privateFolderFlow.value.copy(
                    enabled = privateFolderEnabled,
                    includeNsfw = privateFolderIncludeNsfw
                )

                viewModel.uiState.test {

                    // When
                    val initialState = awaitItem()

                    // Then
                    initialState.rewindValue shouldBe rewindValue
                    initialState.forwardValue shouldBe forwardValue
                    initialState.prefetchHdImages shouldBe prefetchHdImages
                    initialState.privateFolderEnabled shouldBe privateFolderEnabled
                    initialState.privateFolderIncludeNsfw shouldBe privateFolderIncludeNsfw
                    initialState.pipIsEnabled shouldBe pipIsEnabled
                    initialState.autoKeyboard shouldBe autoKeyboard
                    initialState.useExternalPlayer shouldBe externalPlayer
                }
            }
        }

    }

    context("Navigation") {

        test("OnBackTap should emits BackToPreviousScreen") {
            viewModel.event.test {
                viewModel.handleIntent(SettingsIntent.OnBackTap)
                awaitItem() shouldBe SettingsEvent.BackToPreviousScreen
            }
        }

        test("OnTokenTap should emits NavigateToTokenScreen") {
            viewModel.event.test {
                viewModel.handleIntent(SettingsIntent.OnTokenTap)
                awaitItem() shouldBe SettingsEvent.NavigateToTokenScreen
            }
        }

        test("OnAboutTap should emits NavigateToAboutScreen") {
            viewModel.event.test {
                viewModel.handleIntent(SettingsIntent.OnAboutTap)
                awaitItem() shouldBe SettingsEvent.NavigateToAboutScreen
            }
        }

        test("OnHowToTap should emits NavigateToHowToScreen") {
            viewModel.event.test {
                viewModel.handleIntent(SettingsIntent.OnHowToTap)
                awaitItem() shouldBe SettingsEvent.NavigateToHowToScreen
            }
        }

        test("OnCustomizationClick should emits NavigateToCustomizationScreen") {
            viewModel.event.test {
                viewModel.handleIntent(SettingsIntent.OnCustomizationClick)
                awaitItem() shouldBe SettingsEvent.NavigateToCustomizationScreen
            }
        }


    }

    context("Sync catalog") {

        test("ShowSettingsDialog with SYNC_CATALOG value should show dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.ShowSettingsDialog(dialog = SettingsDialog.SYNC_CATALOG))

                // Then
                awaitItem().settingsDialog shouldBe SettingsDialog.SYNC_CATALOG

            }
        }

        test("ShowSettingsDialog with null value should hide dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()
                viewModel.handleIntent(SettingsIntent.ShowSettingsDialog(dialog = SettingsDialog.SYNC_CATALOG))

                // When
                viewModel.handleIntent(SettingsIntent.ShowSettingsDialog(dialog = null))

                // Then
                val state = expectMostRecentItem()
                state.settingsDialog shouldBe null
            }
        }

        test("ProceedFullSync should hide dialog and call syncCatalogUseCase") {

            // Given
            viewModel.uiState.test {
                viewModel.handleIntent(SettingsIntent.ShowSettingsDialog(dialog = SettingsDialog.SYNC_CATALOG))
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.ProceedFullSync)

                // Then
                val state = expectMostRecentItem()
                state.settingsDialog shouldBe null
                coVerify { syncCatalogUseCase(onlyNew = false) }
            }
        }

    }

    context("Dialog") {

        test("ShowRewindDialog should show a FluxOptionsDialogState<Int, SettingsIntent>") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.ShowRewindDialog)

                // Then
                val dialogState = awaitItem().optionsDialog
                dialogState shouldNotBe null
                dialogState.shouldBeInstanceOf<FluxOptionsDialogState<Int, SettingsIntent>>()
            }

        }

        test("ShowForwardDialog should show a FluxOptionsDialogState<Int, SettingsIntent>") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.ShowForwardDialog)

                // Then
                val dialogState = awaitItem().optionsDialog
                dialogState shouldNotBe null
                dialogState.shouldBeInstanceOf<FluxOptionsDialogState<Int, SettingsIntent>>()
            }

        }

        test("ShowLanguageDialog should show a FluxOptionsDialogState<Locale?, SettingsIntent>") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.ShowForwardDialog)

                // Then
                val dialogState = awaitItem().optionsDialog
                dialogState shouldNotBe null
                dialogState.shouldBeInstanceOf<FluxOptionsDialogState<Int, SettingsIntent>>()
            }

        }

        test("HideDialog should hide dialog") {

            // Given
            viewModel.uiState.test {
                viewModel.handleIntent(SettingsIntent.ShowRewindDialog)
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.HideDialog)

                // Then
                val state = expectMostRecentItem()
                state.optionsDialog shouldBe null

            }

        }

    }

    context("Setters") {

        test("SetRewindValue should set value in datastore and then close dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.SetRewindValue(20))
                dataStoreFlow.value = dataStoreFlow.value.copy(playerRewindValue = 20)

                // Then
                val state = awaitItem()
                coVerify { settingsDataStore.setPlayerRewindValue(20) }
                state.rewindValue shouldBe 20
                state.optionsDialog shouldBe null

                cancelAndConsumeRemainingEvents()

            }

        }

        test("SetForwardValue should set value in datastore and then close dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.SetForwardValue(20))
                dataStoreFlow.value = dataStoreFlow.value.copy(playerForwardValue = 20)

                // Then
                val state = awaitItem()
                coVerify { settingsDataStore.setPlayerForwardValue(20) }
                state.forwardValue shouldBe 20
                state.optionsDialog shouldBe null

                cancelAndConsumeRemainingEvents()

            }

        }

        test("SetLanguageValue should set value in datastore and then close dialog") {

            // Given
            val language = "fr"
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.SetLanguageValue(language))
                dataStoreFlow.value = dataStoreFlow.value.copy(dataLanguage = language)

                // Then
                val state = awaitItem()
                coVerify { settingsDataStore.setDataLanguage(language) }
                state.languageValue shouldBe language
                state.optionsDialog shouldBe null

                cancelAndConsumeRemainingEvents()

            }
        }

        test("SetLanguageValue should set value in datastore and then close dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.SetLanguageValue(null))
                dataStoreFlow.value = dataStoreFlow.value.copy(dataLanguage = null)

                // Then
                val state = awaitItem()
                coVerify { settingsDataStore.setDataLanguage(null) }
                state.languageValue shouldBe null
                state.optionsDialog shouldBe null

                cancelAndConsumeRemainingEvents()

            }
        }

        test("OnAutoKeyboardCheck should set value in datastore and then close dialog") {

            // Given
            dataStoreFlow.value = dataStoreFlow.value.copy(autoKeyboard = true)
            viewModel.uiState.test {
                val initialState = awaitItem()
                initialState.autoKeyboard shouldBe true

                // When
                viewModel.handleIntent(SettingsIntent.OnAutoKeyboardCheck(false))
                dataStoreFlow.value = dataStoreFlow.value.copy(autoKeyboard = false)

                // Then
                val state = awaitItem()
                coVerify { settingsDataStore.setAutoKeyboard(false) }
                state.autoKeyboard shouldBe false

                cancelAndConsumeRemainingEvents()

            }
        }

        test("OnExternalPlayerCheck should set value in datastore and then close dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.OnExternalPlayerCheck(true))
                dataStoreFlow.value = dataStoreFlow.value.copy(externalPlayer = true)

                // Then
                val state = awaitItem()
                coVerify { settingsDataStore.setExternalPlayer(true) }
                state.useExternalPlayer shouldBe true

                cancelAndConsumeRemainingEvents()

            }
        }

        test("OnExternalPlayerCheck(true) should emit RequestExternalPlayerPermission event") {

            // Given
            viewModel.event.test {

                // When
                viewModel.handleIntent(SettingsIntent.OnExternalPlayerCheck(true))

                // Then
                awaitItem() shouldBe SettingsEvent.RequestExternalPlayerPermission
            }

        }

        test("OnExternalPlayerCheck(false) should do nothing") {

            // Given
            viewModel.event.test {

                // When
                viewModel.handleIntent(SettingsIntent.OnExternalPlayerCheck(false))

                // Then
                expectNoEvents()
            }

        }

        test("OnEnablePipCheck should set value in datastore") {

            checkAll(
                Exhaustive.boolean()
            ) { check ->

                // Given
                viewModel.uiState.test {
                    awaitItem()

                    // When
                    viewModel.handleIntent(SettingsIntent.OnEnablePipCheck(check))

                    // Then
                    coVerify { settingsDataStore.setEnablePip(check) }
                    cancelAndConsumeRemainingEvents()

                }

            }

        }

        test("OnPrefetchHdImagesCheck should set value in datastore and prefetch images if needed") {

            checkAll(
                Exhaustive.boolean()
            ) { check ->

                clearMocks(settingsDataStore, imagesPrefetchManager)

                // Given
                viewModel.uiState.test {
                    awaitItem()

                    // When
                    viewModel.handleIntent(SettingsIntent.OnPrefetchHdImagesCheck(check))

                    // Then
                    coVerify { settingsDataStore.setPrefetchHdImages(check) }
                    coVerify(exactly = if (check) 1 else 0) { imagesPrefetchManager.prefetchImages() }
                    cancelAndConsumeRemainingEvents()

                }

            }

        }

    }

    context("Private folder") {

        test("enable should show pin creation dialog") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(true))

                //Then
                val state = awaitItem()
                state.privateFolderPinDialog shouldBe PrivateFolderPinDialog.CREATE
            }
        }

        test("disable should show pin verification dialog") {

            // Given
            privateFolderFlow.value = PrivateFolderDataStore.State(enabled = true)
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(false))

                // Then
                val state = awaitItem()
                state.privateFolderPinDialog shouldBe PrivateFolderPinDialog.VERIFY_TO_DISABLE
            }
        }

        test("submit pin when uncheck should enable folder") {

            // Given
            viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(true))
            viewModel.event.test {

                // When
                viewModel.handleIntent(SettingsIntent.SubmitPrivateFolderPin(pin = "1234"))

                // Then
                awaitItem() shouldBe SettingsEvent.PrivateFolderPinUpdated
            }

            coVerify { enablePrivateFolderUseCase("1234") }
        }


        test("submit valid pin when check should disable folder") {

            // Given
            coEvery { disablePrivateFolderUseCase(any()) } returns true
            viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(false))
            viewModel.event.test {

                // When
                viewModel.handleIntent(SettingsIntent.SubmitPrivateFolderPin(pin = "1234"))

                // Then
                awaitItem() shouldBe SettingsEvent.PrivateFolderPinUpdated
            }

            coVerify { disablePrivateFolderUseCase("1234") }
        }

        test("submit wrong pin when uncheck should show error and keep dialog") {

            // Given
            coEvery { disablePrivateFolderUseCase("0000") } returns false
            viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(false))
            viewModel.event.test {

                // When
                viewModel.handleIntent(SettingsIntent.SubmitPrivateFolderPin(pin = "0000"))

                // Then
                expectNoEvents()
            }

            viewModel.uiState.test {
                val state = awaitItem()
                state.privateFolderPinError shouldBe true
                state.privateFolderPinDialog shouldBe PrivateFolderPinDialog.VERIFY_TO_DISABLE
            }

            coVerify(exactly = 1) { disablePrivateFolderUseCase("0000") }
        }

        test("non valid pin is not submitted") {

            // Given
            viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(true))
            viewModel.event.test {

                // When
                viewModel.handleIntent(SettingsIntent.SubmitPrivateFolderPin(pin = "12"))

                // Then
                expectNoEvents()
            }

            coVerify(exactly = 0) { enablePrivateFolderUseCase(any<String>()) }
        }

        test("clear pin error should remove the error") {

            // Given
            coEvery { disablePrivateFolderUseCase(any()) } returns false
            viewModel.handleIntent(SettingsIntent.OnPrivateFolderCheck(false))
            viewModel.handleIntent(SettingsIntent.SubmitPrivateFolderPin(pin = "0000"))
            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(SettingsIntent.ClearPrivateFolderPinError)

                // Then
                awaitItem().privateFolderPinError shouldBe false
            }
        }

        test("OnPrivateFolderIncludeNsfwCheck should call setIncludeNsfwUseCase if needed") {

            // Given
            checkAll(
                Exhaustive.boolean()
            ) { enable ->

                // When
                viewModel.handleIntent(SettingsIntent.OnPrivateFolderIncludeNsfwCheck(enable))

                // Then
                coVerify(exactly = 1) { setIncludeNsfwUseCase(includeNsfw = enable) }

            }
        }

        test("UI state should reflect include nsfw") {

            // Given
            viewModel.uiState.test {
                awaitItem()

                // When
                privateFolderFlow.value = PrivateFolderDataStore.State(enabled = true, includeNsfw = false)

                // Then
                val state = awaitItem()
                state.privateFolderIncludeNsfw shouldBe false
            }
        }

    }

})
