package com.kaem.flux.screens.player.composables.settings

import androidx.compose.runtime.Composable
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState

@Composable
fun PlayerSettings(
    settingsSheet: PlayerUiState.SettingsSheet?,
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

    when (settingsSheet) {
        PlayerUiState.SettingsSheet.Settings -> {
            PlayerSettingsSheet(
                tracksState = tracksState,
                sendIntent = sendIntent
            )
        }
        is PlayerUiState.SettingsSheet.Tracks -> {
            PlayerTracksSheet(
                tracksState = tracksState,
                type = settingsSheet.type,
                sendIntent = sendIntent
            )
        }
        else -> {}
    }

}