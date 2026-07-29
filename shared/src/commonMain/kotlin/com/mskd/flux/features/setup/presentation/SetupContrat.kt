package com.mskd.flux.features.setup.presentation

import androidx.compose.runtime.Immutable

object SetupContrat {
    enum class Screen { WELCOME, SOURCES }
    enum class SourcesOption { DEFAULT, CUSTOM }
}

@Immutable
data class SetupUiState(
    val screen: SetupContrat.Screen = SetupContrat.Screen.WELCOME,
    val sourcesOption: SetupContrat.SourcesOption = SetupContrat.SourcesOption.DEFAULT
)

sealed interface SetupIntent {
    data object OnNextButton: SetupIntent
    data class SelectSourcesOption(val option: SetupContrat.SourcesOption): SetupIntent
    data object OnPermissionGranted: SetupIntent
}

sealed interface SetupEvent {
    data object ShowPermissionDialog: SetupEvent
    data object NavigateToToken: SetupEvent
    data object NavigateToSources: SetupEvent
}