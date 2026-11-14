package com.mskd.flux.screens.player.composables.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerOverlays
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.features.player.presentation.SettingsSheet
import com.mskd.flux.screens.player.composables.collectSelectedAsState
import com.mskd.flux.features.player.ui.PlayerMediaInfoSheet
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerSettings(
    session: PlayerSession<Any>,
    interfaceState: StateFlow<PlayerInterfaceState>,
    overlays: StateFlow<PlayerOverlays>,
    sendIntent: (PlayerIntent) -> Unit
) {

    val settingsSheet by interfaceState.collectSelectedAsState { it.settingsSheet }
    val currentChapter by overlays.collectSelectedAsState { it.currentChapter }

    when (val sheet = settingsSheet) {
        SettingsSheet.Settings -> {
            PlayerSettingsSheet(
                selectedAudio = session.selectedAudio,
                selectedSubtitles = session.selectedSubtitles,
                chapters = session.chapters.takeIf { session.showChapters },
                currentChapter = currentChapter,
                playbackSpeed = session.playbackSpeed,
                subtitleDelayMs = session.subtitleDelayMs,
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
        SettingsSheet.Chapters -> {
            PlayerChaptersSheet(
                chapters = session.chapters,
                currentChapter = currentChapter,
                sendIntent = sendIntent
            )
        }
        SettingsSheet.PlaybackSpeed -> {
            PlayerPlaybackSpeedSheet(
                selectedSpeed = session.playbackSpeed,
                sendIntent = sendIntent
            )
        }
        SettingsSheet.SubtitleDelay -> {
            PlayerSubtitleDelaySheet(
                delayMs = session.subtitleDelayMs,
                sendIntent = sendIntent
            )
        }
        SettingsSheet.MediaInfo -> {
            PlayerMediaInfoSheet(
                mediaInfo = session.mediaInfo,
                onBack = { sendIntent(PlayerIntent.ShowSettings(sheet = SettingsSheet.Settings)) }
            )
        }
        null -> {}
    }

}