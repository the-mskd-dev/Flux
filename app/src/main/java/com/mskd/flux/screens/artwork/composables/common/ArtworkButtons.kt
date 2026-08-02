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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.FluxTextButton
import com.mskd.flux.ui.component.global.ProgressStatusBar
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.content_unavailable
import flux.shared.generated.resources.mark_as_not_watched
import flux.shared.generated.resources.mark_as_watched
import flux.shared.generated.resources.play
import flux.shared.generated.resources.remaining_time
import flux.shared.generated.resources.resume
import flux.shared.generated.resources.rewatch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtworkButtons(
    modifier: Modifier = Modifier,
    media: Media,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val buttonHeight = ButtonDefaults.MediumContainerHeight

    val text = when (media.status) {
        Status.WATCHED -> stringResource(Res.string.rewatch)
        Status.IS_WATCHING -> stringResource(Res.string.resume)
        else -> stringResource(if (media.isAvailable) Res.string.play else Res.string.content_unavailable)
    }

    Column(
        modifier = modifier.width(250.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        MediaStatusProgression(
            modifier = Modifier.fillMaxWidth(),
            media = media
        )

        ToggleButton(
            modifier = Modifier
                .padding(top = FluxUI.Space.small)
                .height(buttonHeight)
                .fillMaxWidth(),
            checked = media.status == Status.WATCHED,
            enabled = media.isAvailable,
            onCheckedChange = { sendIntent(ArtworkIntent.PlayMedia(media)) },
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shapes = ToggleButtonDefaults.shapes(
                shape = CircleShape,
                pressedShape = MaterialTheme.shapes.medium,
                checkedShape = MaterialTheme.shapes.small,
            ),
            content = {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.iconSpacingFor(buttonHeight))
                ) {
                    if (media.isAvailable) {
                        Icon(
                            modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight)),
                            imageVector = if (media.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = "Play button"
                        )
                    }
                    androidx.compose.material3.Text(
                        text = text,
                        style = ButtonDefaults.textStyleFor(buttonHeight)
                    )
                }

            }
        )


        FluxTextButton(
            text = stringResource(if (media.status == Status.WATCHED) Res.string.mark_as_not_watched else Res.string.mark_as_watched),
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
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
            verticalAlignment = Alignment.CenterVertically
        ){

            ProgressStatusBar(
                modifier = Modifier.weight(1f),
                isVisible = true,
                progress = { media.progressPercent }
            )

            val remainingTime = (media.duration.minToMs - media.currentTime).timeDescription(withoutSeconds = true)
            Text.Card.Label(
                text = stringResource(Res.string.remaining_time, remainingTime),
                color = MaterialTheme.colorScheme.onBackground
            )

        }

    }

}

@FluxPreview
@Composable
fun ArtworkButtons_Preview() {
    FluxTheme {
        ArtworkButtons(
            media = MediaMockups.episode1,
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkButtonsWatching_Preview() {
    FluxTheme {
        ArtworkButtons(
            media = MediaMockups.episode1.copy(
                currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
                status = Status.IS_WATCHING
            ),
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkButtonsWatched_Preview() {
    FluxTheme {
        ArtworkButtons(
            media = MediaMockups.episode1.copy(status = Status.WATCHED),
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkButtonsUnavailable_Preview() {
    FluxTheme {
        ArtworkButtons(
            media = MediaMockups.episode1.copy(isAvailable = false),
            sendIntent = {}
        )
    }
}