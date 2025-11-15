package com.kaem.flux.screens.media.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                .padding(top = Ui.Space.LARGE.times(2))
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            media = media,
            onTap = { sendIntent(MediaIntent.ShowPlayer) }
        )

        MediaStatusButton(
            modifier = Modifier
                .padding(top = Ui.Space.SMALL)
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            media = media,
            onTap = { sendIntent(MediaIntent.ChangeWatchStatus(checkPrevious = true)) }
        )

    }

}

@Composable
fun MediaPlayerButton(
    modifier: Modifier,
    media: Media?,
    onTap: () -> Unit
) {

    media ?: return

    val backgroundColor by animateColorAsState(
        targetValue = if (media.status == Status.WATCHED) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
        label = "MediaPlayerButton backgroundColor animation"
    )

    val textColor by animateColorAsState(
        targetValue = if (media.status == Status.WATCHED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary,
        label = "MediaPlayerButton backgroundColor animation"
    )

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

        FluxButton(
            modifier = Modifier.fillMaxWidth(),
            text = text.uppercase(),
            onTap = onTap,
            icon = if (media.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow,
            backgroundColor = backgroundColor,
            textColor = textColor
        )

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

@Composable
fun MediaStatusButton(
    modifier: Modifier,
    media: Media?,
    onTap: () -> Unit
) {

    media ?: return

    AnimatedContent(
        modifier = modifier,
        targetState = (if (media.status == Status.WATCHED) stringResource(R.string.mark_as_not_watched) else stringResource(R.string.mark_as_watched)).uppercase(),
        contentAlignment = Alignment.Center,
        label = "MediaStatusButton animation"
    ) { text ->
        FluxTextButton(
            text = text,
            onTap = onTap
        )
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