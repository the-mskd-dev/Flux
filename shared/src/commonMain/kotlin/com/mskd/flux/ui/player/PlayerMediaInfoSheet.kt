package com.mskd.flux.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.features.player.domain.model.MediaInfo
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.audio_codec
import flux.shared.generated.resources.bitrate
import flux.shared.generated.resources.channels
import flux.shared.generated.resources.container
import flux.shared.generated.resources.frame_rate
import flux.shared.generated.resources.media_info
import flux.shared.generated.resources.resolution
import flux.shared.generated.resources.sampling_rate
import flux.shared.generated.resources.video_codec
import org.jetbrains.compose.resources.stringResource
import kotlin.math.pow
import kotlin.math.roundToLong

/** Read-only technical description of the Media. Back returns to the Settings sheet. */
@Composable
fun PlayerMediaInfoSheet(
    mediaInfo: MediaInfo?,
    onBack: () -> Unit,
) {

    BasicAlertDialog(
        modifier = Modifier.clip(AlertDialogDefaults.shape),
        onDismissRequest = onBack,
    ) {

        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {

            Column(modifier = Modifier.fillMaxWidth()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = FluxUI.Space.small, end = FluxUI.Space.medium, top = FluxUI.Space.small, bottom = FluxUI.Space.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                ) {

                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "back button"
                        )
                    }

                    Text.Content.Title(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.media_info),
                    )

                }

                HorizontalDivider(modifier = Modifier.fillMaxWidth())

                MediaInfoRow(
                    label = stringResource(Res.string.container),
                    value = mediaInfo?.container
                )
                MediaInfoRow(
                    label = stringResource(Res.string.video_codec),
                    value = mediaInfo?.videoCodec
                )
                MediaInfoRow(
                    label = stringResource(Res.string.resolution),
                    value = if (mediaInfo?.width != null && mediaInfo.height != null) "${mediaInfo.width}x${mediaInfo.height}" else null
                )
                MediaInfoRow(
                    label = stringResource(Res.string.frame_rate),
                    value = mediaInfo?.frameRate?.let { "${it.toDouble().trimmed(decimals = 3)} fps" }
                )
                MediaInfoRow(
                    label = stringResource(Res.string.bitrate),
                    value = mediaInfo?.bitrate?.let { "${(it / 1_000_000.0).trimmed(decimals = 1)} Mb/s" }
                )
                MediaInfoRow(
                    label = stringResource(Res.string.audio_codec),
                    value = mediaInfo?.audioCodec
                )
                MediaInfoRow(
                    label = stringResource(Res.string.channels),
                    value = mediaInfo?.channels?.toString()
                )
                MediaInfoRow(
                    label = stringResource(Res.string.sampling_rate),
                    value = mediaInfo?.sampleRate?.let { "${(it / 1_000.0).trimmed(decimals = 1)} kHz" }
                )

            }

        }

    }

}

/** Shown in place of a value that could not be read, so a missing value never looks like a bug. */
private const val PLACEHOLDER = "—"

@Composable
private fun MediaInfoRow(
    label: String,
    value: String?,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = FluxUI.Space.medium),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text.List.Title(text = label)

        Text.List.Body(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = value ?: PLACEHOLDER,
        )

    }

}

/** "23.976", "24", "4.5": trailing zeros removed. */
private fun Double.trimmed(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    return ((this * factor).roundToLong() / factor).toString().removeSuffix(".0")
}

@FluxPreview
@Composable
fun PlayerMediaInfoSheet_Preview() {
    FluxTheme {
        PlayerMediaInfoSheet(
            mediaInfo = MediaInfo(
                container = "MKV",
                videoCodec = "H.264",
                width = 1920,
                height = 1080,
                frameRate = 23.976f,
                bitrate = 4_500_000,
                audioCodec = "E-AC-3",
                channels = 6,
                sampleRate = 48_000,
            ),
            onBack = {},
        )
    }
}

@FluxPreview
@Composable
fun PlayerMediaInfoSheet_UnknownValuesPreview() {
    FluxTheme {
        PlayerMediaInfoSheet(mediaInfo = MediaInfo(videoCodec = "H.264"), onBack = {})
    }
}
