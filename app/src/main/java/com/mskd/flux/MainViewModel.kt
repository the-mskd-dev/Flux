package com.mskd.flux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.tmdb.token.TokenProvider
import com.mskd.flux.navigation.Route
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

    fun getStartingScreen(permissionsGranted: Boolean) : Route {
        return when {
            !permissionsGranted ->
                Route.Welcome
            tokenProvider.tokenRequested ->
                Route.Library
            else ->
                Route.Token(fromSettings = false)
        }
    }

}