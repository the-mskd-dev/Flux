package com.kaem.flux.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import com.kaem.flux.ui.theme.Ui
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val backwardValue: Int = 10,
    val showBackwardDialog: Boolean = false,
    val forwardValue: Int = 10,
    val showForwardDialog: Boolean = false,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val showUiThemeDialog: Boolean = false,
    val subtitlesLanguage: Locale = Locale.getDefault(),
    val showSubtitlesLanguage: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    //region Variables

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

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

    //region Player settings

    fun showBackwardDialog(show: Boolean) {
        _uiState.update {
            it.copy(showBackwardDialog = show)
        }
    }

    fun setBackwardValue(value: Int) = viewModelScope.launch {
        dataStoreRepository.setPlayerBackwardValue(value)
    }

    fun showForwardDialog(show: Boolean) {
        _uiState.update {
            it.copy(showForwardDialog = show)
        }
    }

    fun setForwardValue(value: Int) = viewModelScope.launch {
        dataStoreRepository.setPlayerForwardValue(value)
    }

    //endregion

    //region UI Theme

    fun showUiThemeDialog(show: Boolean) {
        _uiState.update {
            it.copy(showUiThemeDialog = show)
        }
    }

    fun setUiTheme(theme: Ui.THEME) = viewModelScope.launch {
        dataStoreRepository.setUiTheme(theme)
    }

    //endregion

    //region Languages

    fun showSubtitlesLanguageDialog(show: Boolean) {
        _uiState.update {
            it.copy(showSubtitlesLanguage = show)
        }
    }

    fun setSubtitlesLanguage(locale: Locale) = viewModelScope.launch {
        dataStoreRepository.setSubtitlesLanguage(locale)
    }

    //endregion

    companion object {

        val playerSeconds = mapOf(
            5 to "5sec",
            10 to "10sec",
            15 to "15sec",
            20 to "20sec",
            25 to "25sec",
            30 to "30sec",
        )

        val languages = mapOf(
            Locale.ENGLISH to Locale.ENGLISH.displayLanguage,
            Locale.FRENCH to Locale.FRENCH.displayLanguage,
            Locale.ITALIAN to Locale.ITALIAN.displayLanguage,
            Locale.JAPANESE to Locale.JAPANESE.displayLanguage,
            Locale.CHINESE to Locale.CHINESE.displayLanguage,
            Locale.KOREAN to Locale.KOREAN.displayLanguage
        )

    }

}