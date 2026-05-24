package com.mskd.flux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.customization.CustomizationRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.tmdb.token.TokenRepository
import com.mskd.flux.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val customizationRepository: CustomizationRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(SettingsRepository.State())
    val settings = _settings.asStateFlow()

    private val _customization = MutableStateFlow(CustomizationRepository.State())
    val customization = _customization.asStateFlow()

    init {

        viewModelScope.launch {
            settingsRepository.flow.collect { preferences ->
                _settings.update { preferences }
            }
        }

        viewModelScope.launch {
            customizationRepository.flow.collect { preferences ->
                _customization.update { preferences }
            }
        }

    }

    fun getStartingScreen(permissionsGranted: Boolean) : Route {
        return when {
            !permissionsGranted ->
                Route.Welcome
            tokenRepository.tokenRequested ->
                Route.Token(fromSettings = false)
            else ->
                Route.Library
        }
    }

}