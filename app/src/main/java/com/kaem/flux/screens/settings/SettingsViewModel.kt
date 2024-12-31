package com.kaem.flux.screens.settings

import androidx.lifecycle.ViewModel
import com.kaem.flux.data.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val backwardValue: Int = 5,
    val showBackwardDialog: Boolean = true,
    val forwardValue: Int = 5,
    val showForwardDialog: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {

    }
}