package com.mskd.flux.screens.player.composables.settings

import androidx.compose.runtime.Composable
import com.mskd.flux.screens.player.PlayerIntent
import com.mskd.flux.screens.player.PlayerUiState

@Composable
fun PlayerSettings(
    controlsState: () -> PlayerUiState.Controls,
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

    val controls = controlsState()

    when (controls.settingsSheet) {
        PlayerUiState.SettingsSheet.Settings -> {
            PlayerSettingsSheet(
                tracksState = tracksState,
                sendIntent = sendIntent
            )
        }
        is PlayerUiState.SettingsSheet.Tracks -> {
            PlayerTracksSheet(
                tracksState = tracksState,
                type = controls.settingsSheet.type,
                sendIntent = sendIntent
            )
        }
        else -> {}
    }

}