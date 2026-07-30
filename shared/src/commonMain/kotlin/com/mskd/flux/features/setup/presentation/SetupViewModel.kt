package com.mskd.flux.features.setup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.features.setup.domain.model.SetupScreen
import com.mskd.flux.features.setup.domain.model.SourceSelectionMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel : ViewModel() {

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
            is SetupIntent.SelectSourceSelectionMode -> selectSourceSelectionMode(option = intent.option)
            SetupIntent.OnPermissionGranted -> onPermissionGranted()
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
            currentState.sourceSelectionMode == SourceSelectionMode.DEFAULT -> {
                _event.emit(SetupEvent.ShowPermissionDialog)
            }
            else -> {
                _event.emit(SetupEvent.NavigateToToken)
            }
        }

    }

    private fun selectSourceSelectionMode(option: SourceSelectionMode) {
        _uiState.update {
            it.copy(sourceSelectionMode = option)
        }
    }

    private suspend fun onPermissionGranted() {
        _event.emit(SetupEvent.NavigateToToken)
    }

    //endregion

}