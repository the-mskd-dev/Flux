package com.kaem.flux.screens.media.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxTextButton
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.minToMs
import com.kaem.flux.utils.extensions.timeDescription

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaButtons(
    media: Media?,
    sendIntent: (MediaIntent) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        MediaPlayerButton(
            modifier = Modifier
                .width(250.dp)
                .fillMaxWidth(),
            media = media,
            sendIntent = sendIntent
        )

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaPlayerButton(
    modifier: Modifier,
    media: Media?,
    sendIntent: (MediaIntent) -> Unit
) {

    media ?: return

    val text = when (media.status) {
        Status.WATCHED -> stringResource(R.string.rewatch)
        Status.IS_WATCHING -> stringResource(R.string.resume)
        else -> stringResource(R.string.start)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        MediaStatusProgression(media = media)

        Row(
            modifier = Modifier.height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {

            FluxButton(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                text = text.uppercase(),
                onTap = { sendIntent(MediaIntent.ShowPlayer) },
                icon = if (media.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow,
                backgroundColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onPrimary
            )

            MediaStatusButton(
                media = media,
                onTap = { sendIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true)) }
            )

        }

    }

}

@Composable
fun MediaStatusProgression(media: Media) {

    AnimatedVisibility(
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaStatusButton(
    media: Media?,
    onTap: () -> Unit
) {

    media ?: return

    ToggleButton(
        modifier = Modifier.fillMaxHeight(),
        checked = media.status == Status.WATCHED,
        onCheckedChange = { onTap() },
        shapes = ButtonGroupDefaults.connectedMiddleButtonShapes(),
        colors = ToggleButtonDefaults.toggleButtonColors(
            checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            checkedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {

        if (media.status == Status.WATCHED) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = "Mark as not watched button"
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_visibility),
                contentDescription = "Mark as watched button"
            )
        }
    }

}

@Preview
@Composable
fun MediaButtons_Preview() {
    FluxTheme {
        MediaButtons(
            media = MediaMockups.episode1,
            sendIntent = {}
        )
    }
}

@Preview
@Composable
fun MediaButtonsWatching_Preview() {
    FluxTheme {
        MediaButtons(
            media = MediaMockups.episode1.copy(
                currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
                status = Status.IS_WATCHING
            ),
            sendIntent = {}
        )
    }
}

@Preview
@Composable
fun MediaButtonsWatched_Preview() {
    FluxTheme {
        MediaButtons(
            media = MediaMockups.episode1.copy(status = Status.WATCHED),
            sendIntent = {}
        )
    }
}