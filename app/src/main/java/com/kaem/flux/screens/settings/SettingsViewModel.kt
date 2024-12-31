package com.kaem.flux.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val backwardValue: Int = 5,
    val showBackwardDialog: Boolean = false,
    val forwardValue: Int = 5,
    val showForwardDialog: Boolean = false,
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
                        forwardValue = dataStore.playerForwardValue
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

}