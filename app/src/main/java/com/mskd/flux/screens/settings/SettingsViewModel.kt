package com.mskd.flux.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.R
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.ui.component.FluxOptionsDialogItem
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.images.ImagesUC
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val catalogUC: CatalogUC,
    private val imagesUC: ImagesUC,
) : AndroidViewModel(application) {

    //region Variables

    private val context = getApplication<Application>()

    private val _dialogState = MutableStateFlow<FluxOptionsDialogState<*, SettingsIntent>?>(null)
    private val _showFullSyncDialogState = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.flow,
        _dialogState,
        _showFullSyncDialogState,
        catalogUC.state,
        imagesUC.state
    ) { settings, dialog, showSyncDialog, catalog, images ->
        SettingsUiState(
            languageValue = settings.dataLanguage,
            rewindValue = settings.playerRewindValue,
            forwardValue = settings.playerForwardValue,
            useExternalPlayer = settings.externalPlayer,
            autoKeyboard = settings.autoKeyboard,
            dialogState = dialog,
            showSyncDialog = showSyncDialog,
            fullSyncInProgress = (catalog as? CatalogUC.State.Syncing)?.full == true,
            prefetchImages = settings.prefetchImages,
            prefetchImagesState = images
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
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
            SettingsIntent.OnCustomizationTap -> _event.emit(SettingsEvent.NavigateToCustomizationScreen)

            // Dialogs
            SettingsIntent.HideDialog -> hideDialog()
            SettingsIntent.ShowLanguageDialog -> showLanguageDialog()
            SettingsIntent.ShowRewindDialog -> showRewindDialog()
            SettingsIntent.ShowForwardDialog -> showForwardDialog()
            is SettingsIntent.ShowFullSyncDialog -> showFullSyncDialog(show = intent.show)

            // Setters
            is SettingsIntent.SetLanguageValue -> setLanguageValue(intent.value)
            is SettingsIntent.SetRewindValue -> setRewindValue(intent.value)
            is SettingsIntent.SetForwardValue -> setForwardValue(intent.value)

            // Others
            SettingsIntent.ProceedFullSync -> proceedFullSync()
            is SettingsIntent.OnAutoKeyboardCheck -> onAutoKeyboardCheck(value = intent.checked)
            is SettingsIntent.OnExternalPlayerCheck -> onExternalPlayerCheck(value = intent.checked)
            is SettingsIntent.OnPrefetchImagesCheck -> onPrefetchImagesCheck(value = intent.checked)
        }
    }

    //endregion

    private fun hideDialog() {
        _dialogState.update { null }
    }

    private fun showLanguageDialog() {
        val currentValue = uiState.value.languageValue

        val dialogState = FluxOptionsDialogState(
            titleResId = R.string.information_language,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = null, label = context.getString(R.string.system)),
                FluxOptionsDialogItem(value = Locale.ENGLISH, label = Locale.ENGLISH.displayLanguage),
                FluxOptionsDialogItem(value = Locale.FRENCH, label = Locale.FRENCH.displayLanguage),
                FluxOptionsDialogItem(value = Locale.GERMAN , label = Locale.GERMAN.displayLanguage),
                FluxOptionsDialogItem(value = Locale.ITALIAN, label = Locale.ITALIAN.displayLanguage),
                FluxOptionsDialogItem(value = Locale.JAPANESE, label = Locale.JAPANESE.displayLanguage),
                FluxOptionsDialogItem(value = Locale.KOREAN, label = Locale.KOREAN.displayLanguage),
                Locale.forLanguageTag("es").let { FluxOptionsDialogItem(value = it, label = it.displayLanguage) }
            ),
            applyValue = { value -> SettingsIntent.SetLanguageValue(value) }
        )

        _dialogState.update { dialogState }

    }

    private suspend fun setLanguageValue(value: Locale?) {
        settingsRepository.setDataLanguage(value)
        catalogUC.updateLanguage()
        hideDialog()
    }

    private fun showRewindDialog() {
        val currentValue = uiState.value.rewindValue
        val dialogState = FluxOptionsDialogState(
            titleResId = R.string.button_rewind,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = 5, label = "5sec"),
                FluxOptionsDialogItem(value = 10, label = "10sec"),
                FluxOptionsDialogItem(value = 30, label = "30sec")
            ),
            applyValue = { value -> SettingsIntent.SetRewindValue(value) }
        )

        _dialogState.update { dialogState }
    }

    private suspend fun setRewindValue(value: Int) {
        settingsRepository.setPlayerRewindValue(value)
        hideDialog()
    }

    private fun showForwardDialog() {
        val currentValue = uiState.value.forwardValue
        val dialogState = FluxOptionsDialogState(
            titleResId = R.string.button_forward,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = 5, label = "5sec"),
                FluxOptionsDialogItem(value = 10, label = "10sec"),
                FluxOptionsDialogItem(value = 30, label = "30sec")
            ),
            applyValue = { value -> SettingsIntent.SetForwardValue(value) }
        )

        _dialogState.update { dialogState }
    }

    private suspend fun setForwardValue(value: Int) {
        settingsRepository.setPlayerForwardValue(value)
        hideDialog()
    }

    private suspend fun onExternalPlayerCheck(value: Boolean) {

        if (value) {
            _event.emit(SettingsEvent.RequestExternalPlayerPermission)
        }

        settingsRepository.setExternalPlayer(value)
    }

    private suspend fun onAutoKeyboardCheck(value: Boolean) {
        settingsRepository.setAutoKeyboard(value)
    }

    private fun showFullSyncDialog(show: Boolean) {
        _showFullSyncDialogState.update { show }
    }

    private fun proceedFullSync() {
        catalogUC.syncCatalog(onlyNew = false)
        showFullSyncDialog(show = false)
    }

    private suspend fun onPrefetchImagesCheck(value: Boolean) {
        settingsRepository.setPrefetchImages(value)

        if (value)
            imagesUC.prefetchImages()

    }

}