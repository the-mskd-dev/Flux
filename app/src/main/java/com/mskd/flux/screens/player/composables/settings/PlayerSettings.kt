package com.mskd.flux.screens.player.composables.settings

import androidx.compose.runtime.Composable
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerUiContent

@Composable
fun PlayerSettings(
    content: PlayerUiContent<Any>,
    sendIntent: (PlayerIntent) -> Unit
) {

    when (content.settingsSheet) {
        PlayerUiContent.SettingsSheet.Settings -> {
            PlayerSettingsSheet(
                selectedAudio = content.selectedAudio,
                selectedSubtitles = content.selectedSubtitles,
                sendIntent = sendIntent
            )
        }
        is PlayerUiContent.SettingsSheet.Tracks -> {
            PlayerTracksSheet(
                tracks = content.tracks,
                selectedAudio = content.selectedAudio,
                selectedSubtitles = content.selectedSubtitles,
                type = (content.settingsSheet as PlayerUiContent.SettingsSheet.Tracks).type,
                sendIntent = sendIntent
            )
        }
        else -> {}
    }

}