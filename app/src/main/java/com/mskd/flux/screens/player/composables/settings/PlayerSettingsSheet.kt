package com.mskd.flux.screens.player.composables.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.mockups.PlayerMockups
import com.mskd.flux.screens.player.PlayerIntent
import com.mskd.flux.screens.player.PlayerTrack
import com.mskd.flux.screens.player.PlayerUiState
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

    BasicAlertDialog(
        modifier = Modifier
            .clip(AlertDialogDefaults.shape)
            .padding(vertical = Ui.Space.LARGE),
        onDismissRequest = { sendIntent(PlayerIntent.ShowSettings(sheet = null)) },
    ) {

        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                PlayerSettingsItem(
                    label = "Audio",
                    value = tracksState().selectedAudio?.label ?: "None",
                    onTap = {
                        val intent =PlayerUiState.SettingsSheet.Tracks(type = PlayerTrack.Type.AUDIO)
                        sendIntent(PlayerIntent.ShowSettings(sheet = intent))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM))

                PlayerSettingsItem(
                    label = "Subtitles",
                    value = tracksState().selectedSubtitles?.label ?: "None",
                    onTap = {
                        val intent =PlayerUiState.SettingsSheet.Tracks(type = PlayerTrack.Type.SUBTITLES)
                        sendIntent(PlayerIntent.ShowSettings(sheet = intent)) }
                )

            }

        }

    }

}

@Composable
fun PlayerSettingsItem(
    label: String,
    value: String,
    onTap: () -> Unit
) {

    Row(
        modifier = Modifier
            .clickable { onTap() }
            .fillMaxWidth()
            .padding(all = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text.Title.Medium(
            text = label,
        )

        Text.Label.Large(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = value,
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "settings for $label",
        )

    }

}

@FluxPreview
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