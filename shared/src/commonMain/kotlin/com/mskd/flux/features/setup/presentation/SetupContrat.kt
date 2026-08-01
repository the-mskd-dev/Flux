package com.mskd.flux.features.setup.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.features.setup.domain.model.SetupScreen

@Immutable
data class SetupUiState(
    val screen: SetupScreen = SetupScreen.WELCOME,
    val systemFoldersEnabled: Boolean = true
)

sealed interface SetupIntent {
    data object OnNextButton: SetupIntent
    data class EnableSystemFolders(val enabled: Boolean): SetupIntent
    data object OnPermissionGranted: SetupIntent
}

sealed interface SetupEvent {
    data object ShowPermissionDialog: SetupEvent
    data object NavigateToToken: SetupEvent
    data object NavigateToSources: SetupEvent
}