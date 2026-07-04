package com.mskd.flux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.datastore.customization.CustomizationDataStore
import com.mskd.flux.core.datastore.settings.SettingsDataStore
import com.mskd.flux.core.datastore.token.TokenDataStore
import com.mskd.flux.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val customizationDataStore: CustomizationDataStore,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _settings = MutableStateFlow(SettingsDataStore.State())
    val settings = _settings.asStateFlow()

    private val _customization = MutableStateFlow(CustomizationDataStore.State())
    val customization = _customization.asStateFlow()

    init {

        viewModelScope.launch {
            settingsDataStore.flow.collect { preferences ->
                _settings.update { preferences }
            }
        }

        viewModelScope.launch {
            customizationDataStore.flow.collect { preferences ->
                _customization.update { preferences }
            }
        }

    }

    fun getStartingScreen(permissionsGranted: Boolean) : Route {
        return when {
            !permissionsGranted ->
                Route.Welcome
            tokenDataStore.tokenRequested ->
                Route.Token(fromSettings = false)
            else ->
                Route.Library
        }
    }

}