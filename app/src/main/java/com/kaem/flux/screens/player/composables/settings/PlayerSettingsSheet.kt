package com.kaem.flux.screens.player.composables.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.mockups.PlayerMockups
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

}

@Preview
@Composable
fun PlayerSettingsSheet_Preview() {
    AppTheme {
        PlayerSettingsSheet(
            tracksState = {
                PlayerUiState.Tracks(
                    tracks = PlayerMockups.tracks,
                    selectedAudio = PlayerMockups.Audio.japanese,
                    selectedSubtitles = PlayerMockups.Subtitles.french,
                )
            },
            sendIntent = {}
        )
    }
}