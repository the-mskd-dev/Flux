package com.mskd.flux.features.setup.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.features.setup.domain.model.SetupScreen
import com.mskd.flux.features.setup.domain.model.SourceSelectionMode

@Immutable
data class SetupUiState(
    val screen: SetupScreen = SetupScreen.WELCOME,
    val sourceSelectionMode: SourceSelectionMode = SourceSelectionMode.DEFAULT
)

sealed interface SetupIntent {
    data object OnNextButton: SetupIntent
    data class SelectSourceSelectionMode(val option: SourceSelectionMode): SetupIntent
    data object OnPermissionGranted: SetupIntent
}

sealed interface SetupEvent {
    data object ShowPermissionDialog: SetupEvent
    data object NavigateToToken: SetupEvent
    data object NavigateToSources: SetupEvent
}