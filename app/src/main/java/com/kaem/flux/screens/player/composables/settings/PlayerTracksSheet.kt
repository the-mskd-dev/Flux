package com.kaem.flux.screens.player.composables.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.mockups.PlayerMockups
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerTrack
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTracksSheet(
    type: PlayerTrack.Type,
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

    val (tracks, selectedAudio, selectedSubtitles) = tracksState()
    val selectedTrack = if (type == PlayerTrack.Type.AUDIO) selectedAudio else selectedSubtitles

    ModalBottomSheet(
        onDismissRequest = { sendIntent(PlayerIntent.ShowSettings(sheet = null)) }
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {

            itemsIndexed(tracks.filter { it.type == type }) { index, track ->

                if (index != 0)
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = Ui.Space.MEDIUM))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            sendIntent(PlayerIntent.SelectTrack(track = track))
                            sendIntent(PlayerIntent.ShowSettings(sheet = null))
                        }
                        .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.MEDIUM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text.Title.Medium(track.label)

                    if (track == selectedTrack)
                        Icon(Icons.Default.Check, "selected subtitles")
                }

            }
        }

    }

}

@Preview
@Composable
fun PlayerTracksSheet_Preview() {
    AppTheme {
        PlayerTracksSheet(
            tracksState = {
                PlayerUiState.Tracks(
                    tracks = PlayerMockups.tracks,
                    selectedAudio = PlayerMockups.Audio.japanese,
                    selectedSubtitles = PlayerMockups.Subtitles.french,
                )
            },
            type = PlayerTrack.Type.SUBTITLES,
            sendIntent = {}
        )
    }
}