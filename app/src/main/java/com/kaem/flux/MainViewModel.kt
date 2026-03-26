package com.kaem.flux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaem.flux.data.repository.settings.SettingsRepository
import com.kaem.flux.data.tmdb.token.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val tokenProvider: TokenProvider
) : ViewModel() {

    private val _settings = MutableStateFlow(SettingsRepository.State())
    val settings = _settings.asStateFlow()

    init {

        viewModelScope.launch {
            settingsRepository.flow.collect { preferences ->
                _settings.update { preferences }
            }
        }

    }

}