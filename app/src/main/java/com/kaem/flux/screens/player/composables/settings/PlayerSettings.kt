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
        PlayerUiState.SettingsSheet.SETTINGS -> {
            PlayerSettingsSheet(
                tracksState = tracksState,
                sendIntent = sendIntent
            )
        }
        PlayerUiState.SettingsSheet.TRACKS -> {
            PlayerTracksSheet(
                tracksState = tracksState,
                sendIntent = sendIntent
            )
        }
        else -> {}
    }

}