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
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerTrack
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerTracksSheet(
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

    val (tracks, selectedAudio, selectedSubtitles) = tracksState()

    ModalBottomSheet(
        onDismissRequest = { sendIntent(PlayerIntent.ShowSettings) }
    ) {
        // Sheet content
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {

            itemsIndexed(tracks.filter { it.type == PlayerTrack.Type.SUBTITLES }) { index, track ->

                if (index != 0)
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = Ui.Space.MEDIUM))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            sendIntent(PlayerIntent.SelectTrack(track = track))
                            sendIntent(PlayerIntent.ShowSettings)
                        }
                        .padding(horizontal = Ui.Space.MEDIUM, vertical = Ui.Space.MEDIUM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text.Headline.Small(track.label)

                    if (track == selectedSubtitles)
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
                    tracks = listOf(
                        PlayerTrack(id = "1", label = "English", type = PlayerTrack.Type.SUBTITLES),
                        PlayerTrack(id = "2", label = "French", type = PlayerTrack.Type.SUBTITLES),
                        PlayerTrack(id = "3", label = "German", type = PlayerTrack.Type.SUBTITLES),
                        PlayerTrack(id = "4", label = "Italian", type = PlayerTrack.Type.SUBTITLES),
                    ),
                    selectedAudio = null,
                    selectedSubtitles = PlayerTrack(
                        id = "2",
                        label = "French",
                        type = PlayerTrack.Type.SUBTITLES
                    ),
                )
            },
            sendIntent = {}
        )
    }
}