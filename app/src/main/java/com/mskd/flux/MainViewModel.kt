package com.mskd.flux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val customizationDataStore: CustomizationDataStore,
    private val tokenDataStore: TokenDataStore,
    private val userDataStore: UserDataStore,
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

    fun getStartingScreen() : Route {

        val versionCode = runBlocking { userDataStore.flow.first().versionCode }

        return when {
            versionCode < 0 -> Route.Setup
            versionCode in 1..27 -> Route.Sources(fromSetup = true) // TODO: Delete in October 2026
            tokenDataStore.tokenRequested -> Route.Token(fromSetup = true)
            else -> Route.Catalog
        }
    }

    fun disableSystemFoldersIfNeeded(permissionsGranted: Boolean) = viewModelScope.launch {
        val versionCode = userDataStore.flow.first().versionCode

        if (versionCode > 0 && !permissionsGranted)
            settingsDataStore.setSystemFolders(enabled = false)
    }

}