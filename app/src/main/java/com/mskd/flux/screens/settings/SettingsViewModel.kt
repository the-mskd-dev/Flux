package com.mskd.flux.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui
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
    private val settingsRepository: SettingsRepository,
    private val catalogUC: CatalogUC,
    private val imagesUC: ImagesUC
) : ViewModel() {

    //region Variables

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
            uiTheme = settings.uiTheme,
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
            SettingsIntent.ShowLanguageDialog -> showLanguageDialog()
            is SettingsIntent.SetLanguageValue -> setLanguageValue(intent.value)
            SettingsIntent.ShowRewindDialog -> showRewindDialog()
            is SettingsIntent.SetRewindValue -> setRewindValue(intent.value)
            SettingsIntent.ShowForwardDialog -> showForwardDialog()
            is SettingsIntent.SetForwardValue -> setForwardValue(intent.value)
            SettingsIntent.ShowThemeDialog -> showThemeDialog()
            is SettingsIntent.SetThemeValue -> setTheme(intent.theme)
            SettingsIntent.HideDialog -> hideDialog()
            SettingsIntent.OnBackTap -> _event.emit(SettingsEvent.BackToPreviousScreen)
            SettingsIntent.OnTokenTap -> _event.emit(SettingsEvent.NavigateToTokenScreen)
            SettingsIntent.OnAboutTap -> _event.emit(SettingsEvent.NavigateToAboutScreen)
            SettingsIntent.OnHowToTap -> _event.emit(SettingsEvent.NavigateToHowToScreen)
            is SettingsIntent.OnAutoKeyboardCheck -> onAutoKeyboardCheck(value = intent.checked)
            is SettingsIntent.OnExternalPlayerCheck -> onExternalPlayerCheck(value = intent.checked)
            is SettingsIntent.ShowFullSyncDialog -> showFullSyncDialog(show = intent.show)
            SettingsIntent.ProceedFullSync -> proceedFullSync()
            is SettingsIntent.OnPrefetchImagesCheck -> onPrefetchImagesCheck(value = intent.checked)
        }
    }

    //endregion

    private fun hideDialog() {
        _dialogState.update { null }
    }

    private fun showLanguageDialog() {
        val currentValue = uiState.value.languageValue
        _dialogState.update { SettingsOptionsDialogs.language(currentValue) }
    }

    private suspend fun setLanguageValue(value: Locale?) {
        settingsRepository.setDataLanguage(value)
        catalogUC.updateLanguage()
        hideDialog()
    }

    private fun showRewindDialog() {
        val currentValue = uiState.value.rewindValue
        _dialogState.update { SettingsOptionsDialogs.rewind(currentValue) }
    }

    private suspend fun setRewindValue(value: Int) {
        settingsRepository.setPlayerRewindValue(value)
        hideDialog()
    }

    private fun showForwardDialog() {
        val currentValue = uiState.value.forwardValue
        _dialogState.update { SettingsOptionsDialogs.forward(currentValue) }
    }

    private suspend fun setForwardValue(value: Int) {
        settingsRepository.setPlayerForwardValue(value)
        hideDialog()
    }

    private fun showThemeDialog() {
        val currentValue = uiState.value.uiTheme
        _dialogState.update { SettingsOptionsDialogs.theme(currentValue) }
    }

    private suspend fun setTheme(theme: Ui.THEME) {
        settingsRepository.setUiTheme(theme)
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