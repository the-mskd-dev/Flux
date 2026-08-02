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
import com.mskd.flux.core.model.player.PlayerTrack
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerUiContent
import com.mskd.flux.mockups.PlayerMockups
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.by_default
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsSheet(
    selectedAudio: PlayerTrack?,
    selectedSubtitles: PlayerTrack?,
    sendIntent: (PlayerIntent) -> Unit
) {

    BasicAlertDialog(
        modifier = Modifier
            .clip(AlertDialogDefaults.shape)
            .padding(vertical = FluxUI.Space.large),
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
                    value = selectedAudio?.label.orEmpty().ifBlank { stringResource(Res.string.by_default) },
                    onTap = {
                        val intent = PlayerUiContent.SettingsSheet.Tracks(type = PlayerTrack.Type.AUDIO)
                        sendIntent(PlayerIntent.ShowSettings(sheet = intent))
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = FluxUI.Space.medium))

                PlayerSettingsItem(
                    label = "Subtitles",
                    value = selectedSubtitles?.label.orEmpty().ifBlank { stringResource(Res.string.by_default) },
                    onTap = {
                        val intent = PlayerUiContent.SettingsSheet.Tracks(type = PlayerTrack.Type.SUBTITLES)
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
            .padding(all = FluxUI.Space.medium),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text.List.Title(
            text = label,
        )

        Text.List.Body(
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
    FluxTheme {
        PlayerSettingsSheet(
            selectedAudio = PlayerMockups.Audio.japanese,
            selectedSubtitles = PlayerMockups.Subtitles.french,
            sendIntent = {}
        )
    }
}