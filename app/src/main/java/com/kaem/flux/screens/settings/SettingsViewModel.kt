package com.kaem.flux.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.SettingsRepository
import com.kaem.flux.ui.theme.Ui
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
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    //region Variables

    private val _dialogState = MutableStateFlow<SettingsDialogState<*>?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.flow,
        _dialogState
    ) { settings, dialog ->
        SettingsUiState(
            backwardValue = settings.playerBackwardValue,
            forwardValue = settings.playerForwardValue,
            uiTheme = settings.uiTheme,
            subtitlesLanguage = settings.subtitlesLanguage,
            dialogState = dialog
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
            SettingsIntent.ShowBackwardDialog -> showBackwardDialog()
            is SettingsIntent.SetBackwardValue -> setBackwardValue(intent.value)
            SettingsIntent.ShowForwardDialog -> showForwardDialog()
            is SettingsIntent.SetForwardValue -> setForwardValue(intent.value)
            SettingsIntent.ShowSubtitlesDialog -> showSubtitlesLanguageDialog()
            is SettingsIntent.SetSubtitlesValue -> setSubtitlesLanguage(intent.locale)
            SettingsIntent.ShowThemeDialog -> showThemeDialog()
            is SettingsIntent.SetThemeValue -> setTheme(intent.theme)
            SettingsIntent.HideDialog -> hideDialog()
            SettingsIntent.OnBackTap -> _event.emit(SettingsEvent.BackToPreviousScreen)
            SettingsIntent.OnAboutTap -> _event.emit(SettingsEvent.NavigateToAboutScreen)
            SettingsIntent.OnHowToTap -> _event.emit(SettingsEvent.NavigateToHowToScreen)
        }
    }

    //endregion

    private fun hideDialog() {
        _dialogState.update { null }
    }

    private fun showBackwardDialog() {
        val currentValue = uiState.value.backwardValue
        _dialogState.update { SettingsDialogState.backward(currentValue) }
    }

    private suspend fun setBackwardValue(value: Int) {
        settingsRepository.setPlayerBackwardValue(value)
        hideDialog()
    }

    private fun showForwardDialog() {
        val currentValue = uiState.value.forwardValue
        _dialogState.update { SettingsDialogState.forward(currentValue) }
    }

    private suspend fun setForwardValue(value: Int) {
        settingsRepository.setPlayerForwardValue(value)
        hideDialog()
    }

    private fun showThemeDialog() {
        val currentValue = uiState.value.uiTheme
        _dialogState.update { SettingsDialogState.theme(currentValue) }
    }

    private suspend fun setTheme(theme: Ui.THEME) {
        settingsRepository.setUiTheme(theme)
        hideDialog()
    }

    private fun showSubtitlesLanguageDialog() {
        val currentValue = uiState.value.subtitlesLanguage
        _dialogState.update { SettingsDialogState.subtitles(currentValue) }
    }

    private suspend fun setSubtitlesLanguage(value: Locale) {
        settingsRepository.setSubtitlesLanguage(value)
        hideDialog()
    }

}