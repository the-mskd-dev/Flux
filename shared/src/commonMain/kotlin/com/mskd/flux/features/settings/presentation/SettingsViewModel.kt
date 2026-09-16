package com.mskd.flux.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.core.FluxOptionsDialogItem
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.domain.usecase.updateLanguage.UpdateLanguageUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.privateFolder.domain.usecase.changePrivateFolderPin.ChangePrivateFolderPinUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.disablePrivateFolder.DisablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.enablePrivateFolder.EnablePrivateFolderUseCase
import com.mskd.flux.features.privateFolder.domain.usecase.observePrivateFolder.ObservePrivateFolderUseCase
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.settings.domain.model.SettingsDialog
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.button_forward
import flux.shared.generated.resources.button_rewind
import flux.shared.generated.resources.information_language
import flux.shared.generated.resources.system
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val imagesPrefetchManager: ImagesPrefetchManager,
    private val syncCatalogUseCase: SyncCatalogUseCase,
    private val updateLanguageUseCase: UpdateLanguageUseCase,
    private val observePrivateFolderUseCase: ObservePrivateFolderUseCase,
    private val enablePrivateFolderUseCase: EnablePrivateFolderUseCase,
    private val disablePrivateFolderUseCase: DisablePrivateFolderUseCase,
    private val changePrivateFolderPinUseCase: ChangePrivateFolderPinUseCase
) : ViewModel() {

    //region Variables

    private val _optionsDialogState = MutableStateFlow<FluxOptionsDialogState<*, SettingsIntent>?>(null)
    private val _settingsDialogState = MutableStateFlow<SettingsDialog?>(null)

    private val _privateFolderPinDialog = MutableStateFlow<PrivateFolderPinDialog?>(null)
    private val _privateFolderPinInput = MutableStateFlow(PrivateFolderPinInput())
    private val _privateFolderPinError = MutableStateFlow(false)

    private val baseState = combine(
        settingsDataStore.flow,
        _optionsDialogState,
        _settingsDialogState,
        syncCatalogUseCase.state,
        imagesPrefetchManager.state
    ) { settings, dialog, settingsDialog, catalog, images ->
        SettingsBaseState(
            settings = settings,
            optionsDialog = dialog,
            settingsDialog = settingsDialog,
            syncState = catalog,
            imagesState = images
        )
    }

    private val privateFolderState = combine(
        observePrivateFolderUseCase.flow,
        _privateFolderPinDialog,
        _privateFolderPinInput,
        _privateFolderPinError
    ) { privateFolder, pinDialog, pinInput, pinError ->
        PrivateFolderSlice(
            enabled = privateFolder.enabled,
            pinDialog = pinDialog,
            pinInput = pinInput,
            pinError = pinError
        )
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        baseState,
        privateFolderState
    ) { base, privateFolder ->
        SettingsUiState(
            languageValue = base.settings.dataLanguage,
            rewindValue = base.settings.playerRewindValue,
            forwardValue = base.settings.playerForwardValue,
            useExternalPlayer = base.settings.externalPlayer,
            pipIsEnabled = base.settings.pipIsEnabled,
            autoKeyboard = base.settings.autoKeyboard,
            optionsDialog = base.optionsDialog,
            settingsDialog = base.settingsDialog,
            fullSyncInProgress = (base.syncState as? SyncState.Syncing)?.full == true,
            prefetchHdImages = base.settings.prefetchHdImages,
            prefetchImagesState = base.imagesState,
            privateFolderEnabled = privateFolder.enabled,
            privateFolderPinDialog = privateFolder.pinDialog,
            privateFolderPinInput = privateFolder.pinInput,
            privateFolderPinError = privateFolder.pinError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    private data class SettingsBaseState(
        val settings: SettingsDataStore.State,
        val optionsDialog: FluxOptionsDialogState<*, SettingsIntent>?,
        val settingsDialog: SettingsDialog?,
        val syncState: SyncState,
        val imagesState: ImagesPrefetchManager.State
    )

    private data class PrivateFolderSlice(
        val enabled: Boolean,
        val pinDialog: PrivateFolderPinDialog?,
        val pinInput: PrivateFolderPinInput,
        val pinError: Boolean
    )

    private val _event = MutableSharedFlow<SettingsEvent>()
    val event = _event.asSharedFlow()

    //endregion

    //region Intents

    fun handleIntent(intent: SettingsIntent) = viewModelScope.launch {
        when (intent) {

            // Navigation
            SettingsIntent.OnBackTap -> _event.emit(SettingsEvent.BackToPreviousScreen)
            SettingsIntent.OnTokenTap -> _event.emit(SettingsEvent.NavigateToTokenScreen)
            SettingsIntent.OnAboutTap -> _event.emit(SettingsEvent.NavigateToAboutScreen)
            SettingsIntent.OnHowToTap -> _event.emit(SettingsEvent.NavigateToHowToScreen)
            SettingsIntent.OnCustomizationClick -> _event.emit(SettingsEvent.NavigateToCustomizationScreen)
            SettingsIntent.OnSourcesTap -> _event.emit(SettingsEvent.NavigateToSourcesScreen)

            // Dialogs
            SettingsIntent.HideDialog -> hideDialog()
            SettingsIntent.ShowLanguageDialog -> showLanguageDialog()
            SettingsIntent.ShowRewindDialog -> showRewindDialog()
            SettingsIntent.ShowForwardDialog -> showForwardDialog()
            is SettingsIntent.ShowSettingsDialog -> showSettingsDialog(dialog = intent.dialog)

            // Setters
            is SettingsIntent.SetLanguageValue -> setLanguageValue(intent.value)
            is SettingsIntent.SetRewindValue -> setRewindValue(intent.value)
            is SettingsIntent.SetForwardValue -> setForwardValue(intent.value)

            // Others
            SettingsIntent.ProceedFullSync -> proceedFullSync()
            is SettingsIntent.OnAutoKeyboardCheck -> onAutoKeyboardCheck(value = intent.checked)
            is SettingsIntent.OnExternalPlayerCheck -> onExternalPlayerCheck(value = intent.checked)
            is SettingsIntent.OnEnablePipCheck -> onEnablePipCheck(value = intent.checked)
            is SettingsIntent.OnPrefetchHdImagesCheck -> onPrefetchImagesCheck(value = intent.checked)

            // Private folder
            is SettingsIntent.OnPrivateFolderCheck -> onPrivateFolderCheck(checked = intent.checked)
            SettingsIntent.ShowChangePinDialog -> showPrivateFolderPinDialog(dialog = PrivateFolderPinDialog.CHANGE_PIN)
            is SettingsIntent.OnPrivateFolderPinChanged -> onPrivateFolderPinChanged(
                primary = intent.primary,
                secondary = intent.secondary
            )
            SettingsIntent.SubmitPrivateFolderPin -> submitPrivateFolderPin()
            SettingsIntent.HidePrivateFolderPinDialog -> hidePrivateFolderPinDialog()
        }
    }

    //endregion

    private fun hideDialog() {
        _optionsDialogState.update { null }
    }

    private suspend fun showLanguageDialog() {
        val currentValue = uiState.value.languageValue

        val dialogState = FluxOptionsDialogState(
            titleResId = Res.string.information_language,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = null, label = StringProvider.Resource(Res.string.system)),
                FluxOptionsDialogItem(value = Locale.ENGLISH, label = StringProvider.Static(Locale.ENGLISH.displayLanguage)),
                FluxOptionsDialogItem(value = Locale.FRENCH, label = StringProvider.Static(Locale.FRENCH.displayLanguage)),
                FluxOptionsDialogItem(value = Locale.GERMAN , StringProvider.Static(label = Locale.GERMAN.displayLanguage)),
                FluxOptionsDialogItem(value = Locale.ITALIAN, StringProvider.Static(label = Locale.ITALIAN.displayLanguage)),
                FluxOptionsDialogItem(value = Locale.JAPANESE, StringProvider.Static(label = Locale.JAPANESE.displayLanguage)),
                FluxOptionsDialogItem(value = Locale.KOREAN, StringProvider.Static(label = Locale.KOREAN.displayLanguage)),
                Locale.forLanguageTag("es").let { FluxOptionsDialogItem(value = it, StringProvider.Static(label = it.displayLanguage)) }
            ),
            applyValue = { value -> SettingsIntent.SetLanguageValue(value) }
        )

        _optionsDialogState.update { dialogState }

    }

    private suspend fun setLanguageValue(value: Locale?) {
        settingsDataStore.setDataLanguage(value)
        updateLanguageUseCase()
        hideDialog()
    }

    private fun showRewindDialog() {
        val currentValue = uiState.value.rewindValue
        val dialogState = FluxOptionsDialogState(
            titleResId = Res.string.button_rewind,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = 5, label = StringProvider.Static("5sec")),
                FluxOptionsDialogItem(value = 10, StringProvider.Static(label = "10sec")),
                FluxOptionsDialogItem(value = 30, label = StringProvider.Static("30sec"))
            ),
            applyValue = { value -> SettingsIntent.SetRewindValue(value) }
        )

        _optionsDialogState.update { dialogState }
    }

    private suspend fun setRewindValue(value: Int) {
        settingsDataStore.setPlayerRewindValue(value)
        hideDialog()
    }

    private fun showForwardDialog() {
        val currentValue = uiState.value.forwardValue
        val dialogState = FluxOptionsDialogState(
            titleResId = Res.string.button_forward,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = 5, label = StringProvider.Static("5sec")),
                FluxOptionsDialogItem(value = 10, label = StringProvider.Static("10sec")),
                FluxOptionsDialogItem(value = 30, label = StringProvider.Static("30sec"))
            ),
            applyValue = { value -> SettingsIntent.SetForwardValue(value) }
        )

        _optionsDialogState.update { dialogState }
    }

    private suspend fun setForwardValue(value: Int) {
        settingsDataStore.setPlayerForwardValue(value)
        hideDialog()
    }

    private suspend fun onExternalPlayerCheck(value: Boolean) {

        if (value) {
            _event.emit(SettingsEvent.RequestExternalPlayerPermission)
        }

        settingsDataStore.setExternalPlayer(value)
    }

    private suspend fun onEnablePipCheck(value: Boolean) {
        settingsDataStore.setEnablePip(value)
    }

    private suspend fun onAutoKeyboardCheck(value: Boolean) {
        settingsDataStore.setAutoKeyboard(value)
    }

    private fun proceedFullSync() {
        syncCatalogUseCase(onlyNew = false)
        showSettingsDialog(null)
    }

    private suspend fun onPrefetchImagesCheck(value: Boolean) {
        settingsDataStore.setPrefetchHdImages(value)

        if (value)
            imagesPrefetchManager.prefetchImages()

    }

    private fun showSettingsDialog(dialog: SettingsDialog?) {
        _settingsDialogState.update { dialog }
    }

    //endregion

    //region Private Folder

    private fun onPrivateFolderCheck(checked: Boolean) {
        if (checked)
            showPrivateFolderPinDialog(dialog = PrivateFolderPinDialog.CREATE)
        else
            showPrivateFolderPinDialog(dialog = PrivateFolderPinDialog.VERIFY_TO_DISABLE)
    }

    private fun showPrivateFolderPinDialog(dialog: PrivateFolderPinDialog) {
        _privateFolderPinInput.update { PrivateFolderPinInput() }
        _privateFolderPinError.update { false }
        _privateFolderPinDialog.update { dialog }
    }

    private fun onPrivateFolderPinChanged(primary: String, secondary: String) {
        val maxLength = PrivateFolderPinInput.PIN_LENGTH

        _privateFolderPinInput.update { current ->
            current.copy(
                primary = primary.filter { it.isDigit() }.take(maxLength),
                secondary = secondary.filter { it.isDigit() }.take(maxLength)
            )
        }
        _privateFolderPinError.update { false }
    }

    private suspend fun submitPrivateFolderPin() {
        val dialog = _privateFolderPinDialog.value ?: return
        val input = _privateFolderPinInput.value

        when (dialog) {
            PrivateFolderPinDialog.CREATE -> {
                if (!input.primaryIsComplete) return

                enablePrivateFolderUseCase(pin = input.primary)
                _event.emit(SettingsEvent.PrivateFolderPinUpdated)
                hidePrivateFolderPinDialog()
            }
            PrivateFolderPinDialog.VERIFY_TO_DISABLE -> {
                if (!input.primaryIsComplete) return

                val pinIsValid = disablePrivateFolderUseCase(pin = input.primary)
                if (pinIsValid) {
                    _event.emit(SettingsEvent.PrivateFolderPinUpdated)
                    hidePrivateFolderPinDialog()
                } else {
                    _privateFolderPinError.update { true }
                }
            }
            PrivateFolderPinDialog.CHANGE_PIN -> {
                if (!input.primaryIsComplete || !input.secondaryIsComplete) return

                val pinIsValid = changePrivateFolderPinUseCase(oldPin = input.primary, newPin = input.secondary)
                if (pinIsValid) {
                    _event.emit(SettingsEvent.PrivateFolderPinUpdated)
                    hidePrivateFolderPinDialog()
                } else {
                    _privateFolderPinError.update { true }
                }
            }
        }
    }

    private fun hidePrivateFolderPinDialog() {
        _privateFolderPinDialog.update { null }
        _privateFolderPinInput.update { PrivateFolderPinInput() }
        _privateFolderPinError.update { false }
    }

    //endregion

}