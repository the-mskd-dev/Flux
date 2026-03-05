package com.kaem.flux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.settings.SettingsPreferences
import com.kaem.flux.data.repository.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _settings = MutableStateFlow(SettingsPreferences())
    val settings = _settings.asStateFlow()

    init {

        viewModelScope.launch {
            settingsRepository.flow.collect { preferences ->
                _settings.update { preferences }
            }
        }
    }

}