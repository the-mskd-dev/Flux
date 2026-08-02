package com.mskd.flux.features.setup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.setup.domain.model.SetupScreen
import com.mskd.flux.utils.Trace
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // region States
    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SetupEvent>()
    val event = _event.asSharedFlow()

    //endregion

    //region Public Methods

    fun handleIntent(intent: SetupIntent) = viewModelScope.launch {
        when(intent) {
            SetupIntent.OnNextButton -> onNextButton()
            is SetupIntent.EnableSystemFolders -> enableSystemFolders(enabled = intent.enabled)
            SetupIntent.OnPermissionGranted -> onPermissionGranted()
        }
    }

    init {

        viewModelScope.launch {
            val systemFoldersEnabled = settingsDataStore.flow.first().systemFoldersEnabled
            Trace.debug("systemFoldersEnabled : $systemFoldersEnabled")
        }

    }

    //endregion

    //region Private Methods

    private suspend fun onNextButton() {

        val currentState = _uiState.value

        when {
            currentState.screen == SetupScreen.WELCOME -> {
                _uiState.update { it.copy(screen = SetupScreen.SOURCES) }
            }
            currentState.systemFoldersEnabled -> {
                _event.emit(SetupEvent.ShowPermissionDialog)
            }
            else -> {
                _event.emit(SetupEvent.NavigateToSources)
            }
        }

    }

    private suspend fun enableSystemFolders(enabled: Boolean) {
        _uiState.update {
            it.copy(systemFoldersEnabled = enabled)
        }

        settingsDataStore.setSystemFolders(enabled = enabled)
    }

    private suspend fun onPermissionGranted() {
        settingsDataStore.setSystemFolders(enabled = true)
        _event.emit(SetupEvent.NavigateToToken)
    }

    //endregion

}