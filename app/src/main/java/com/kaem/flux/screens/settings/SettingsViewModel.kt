package com.kaem.flux.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.ui.theme.Ui
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    //region Variables

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SettingsEvent>()
    val event = _event.asSharedFlow()

    //endregion

    //region Init

    init {

        viewModelScope.launch {
            dataStoreRepository.flow.collect { dataStore ->
                _uiState.update {
                    it.copy(
                        backwardValue = dataStore.playerBackwardValue,
                        forwardValue = dataStore.playerForwardValue,
                        uiTheme = dataStore.uiTheme,
                        subtitlesLanguage = dataStore.subtitlesLanguage
                    )
                }
            }
        }

    }

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
        _uiState.update { it.copy(dialogState = null) }
    }

    private fun showBackwardDialog() {
        _uiState.update {
            it.copy(dialogState = SettingsDialogState.backward(it.backwardValue))
        }
    }

    private suspend fun setBackwardValue(value: Int) {
        dataStoreRepository.setPlayerBackwardValue(value)
    }

    private fun showForwardDialog() {
        _uiState.update {
            it.copy(dialogState = SettingsDialogState.forward(it.forwardValue))
        }
    }

    private suspend fun setForwardValue(value: Int) {
        dataStoreRepository.setPlayerForwardValue(value)
    }

    private fun showThemeDialog() {
        _uiState.update {
            it.copy(dialogState = SettingsDialogState.theme(it.uiTheme))
        }
    }

    private suspend fun setTheme(theme: Ui.THEME) {
        dataStoreRepository.setUiTheme(theme)
    }

    private fun showSubtitlesLanguageDialog() {
        _uiState.update {
            it.copy(dialogState = SettingsDialogState.subtitles(it.subtitlesLanguage))
        }
    }

    private suspend fun setSubtitlesLanguage(value: Locale) {
        dataStoreRepository.setSubtitlesLanguage(value)
    }

}