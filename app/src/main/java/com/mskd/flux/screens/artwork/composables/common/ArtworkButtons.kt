package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.FluxTextButton
import com.mskd.flux.ui.component.ProgressBar
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtworkButtons(
    media: Media,
    hideProgress: Boolean,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val buttonHeight = ButtonDefaults.MediumContainerHeight

    val text = when (media.status) {
        Status.WATCHED -> stringResource(R.string.rewatch)
        Status.IS_WATCHING if !hideProgress -> stringResource(R.string.resume)
        else -> stringResource(R.string.play)
    }

    Column(
        modifier = Modifier.width(250.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        if (!hideProgress) {
            MediaStatusProgression(
                modifier = Modifier.fillMaxWidth(),
                media = media
            )
        }

        ToggleButton(
            modifier = Modifier
                .padding(top = Ui.Space.SMALL)
                .height(buttonHeight)
                .fillMaxWidth(),
            checked = media.status == Status.WATCHED,
            onCheckedChange = { sendIntent(ArtworkIntent.PlayMedia(media)) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = ToggleButtonDefaults.shapes(
                shape = Ui.Shape.Corner.Full,
                pressedShape = Ui.Shape.Corner.Medium,
                checkedShape = Ui.Shape.Corner.Small,
            ),
            content = {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.iconSpacingFor(buttonHeight))
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight)),
                        imageVector = if (media.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = "Play button"
                    )
                    androidx.compose.material3.Text(
                        text = text,
                        style = ButtonDefaults.textStyleFor(buttonHeight)
                    )
                }

            }
        )


        FluxTextButton(
            text = stringResource(if (media.status == Status.WATCHED) R.string.mark_as_not_watched else R.string.mark_as_watched),
            height = buttonHeight,
            onTap = { sendIntent(ArtworkIntent.ChangeWatchStatus(media = media)) }
        )

    }

}

@Composable
fun MediaStatusProgression(
    modifier: Modifier,
    media: Media
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = media.status == Status.IS_WATCHING,
        label = "MediaStatusProgression animation"
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ){

            ProgressBar(
                modifier = Modifier.weight(1f),
                media = media
            )

            val remainingTime = (media.duration.minToMs - media.currentTime).timeDescription(withoutSeconds = true)
            Text.Label.Medium(
                text = stringResource(R.string.remaining_time, remainingTime),
                color = MaterialTheme.colorScheme.onBackground
            )

        }

    }

}

@FluxPreview
@Composable
fun ArtworkButtons_Preview() {
    AppTheme {
        ArtworkButtons(
            media = MediaMockups.episode1,
            hideProgress = false,
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkButtonsWatching_Preview() {
    AppTheme {
        ArtworkButtons(
            media = MediaMockups.episode1.copy(
                currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
                status = Status.IS_WATCHING
            ),
            hideProgress = false,
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkButtonsWatched_Preview() {
    AppTheme {
        ArtworkButtons(
            media = MediaMockups.episode1.copy(status = Status.WATCHED),
            hideProgress = false,
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkButtonsHideProgress_Preview() {
    AppTheme {
        ArtworkButtons(
            media = MediaMockups.episode1.copy(status = Status.WATCHED),
            hideProgress = true,
            sendIntent = {}
        )
    }
}