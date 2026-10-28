package com.mskd.flux.screens.player.composables.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.features.player.presentation.SettingsSheet
import com.mskd.flux.screens.player.composables.collectSelectedAsState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerSettings(
    session: PlayerSession<Any>,
    interfaceState: StateFlow<PlayerInterfaceState>,
    sendIntent: (PlayerIntent) -> Unit
) {

    val settingsSheet by interfaceState.collectSelectedAsState { it.settingsSheet }

    when (val sheet = settingsSheet) {
        SettingsSheet.Settings -> {
            PlayerSettingsSheet(
                selectedAudio = session.selectedAudio,
                selectedSubtitles = session.selectedSubtitles,
                sendIntent = sendIntent
            )
        }
        is SettingsSheet.Tracks -> {
            PlayerTracksSheet(
                tracks = session.tracks,
                selectedAudio = session.selectedAudio,
                selectedSubtitles = session.selectedSubtitles,
                type = sheet.type,
                sendIntent = sendIntent
            )
        }
        null -> {}
    }

}