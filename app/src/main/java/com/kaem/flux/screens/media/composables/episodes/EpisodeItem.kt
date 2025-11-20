package com.kaem.flux.screens.media.composables.episodes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.extensions.formattedText
import com.kaem.flux.utils.extensions.grayScale
import com.kaem.flux.utils.extensions.minToMs
import com.kaem.flux.utils.extensions.timeDescription

@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode,
    sendIntent: (MediaIntent) -> Unit
) {

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .combinedClickable(
                onClick = { sendIntent(MediaIntent.PlayMedia(episode)) },
                onLongClick = { showMenu = true }
            )
            .animateContentSize()
            .fillMaxWidth()
            .padding(Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
        ) {

            EpisodeImage(
                modifier = Modifier.weight(.4f),
                episode = episode,
            )

            Column(
                modifier = Modifier.weight(.6f),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL),
                horizontalAlignment = Alignment.Start
            ) {

                Text.Title.Medium(
                    modifier = Modifier.fillMaxWidth(),
                    text =  "${episode.number}. ${episode.title}",
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    emphasized = true
                )

                Text.Label.Small(
                    modifier = Modifier.fillMaxWidth(),
                    text = episode.releaseDate?.formattedText,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary
                )

                Text.Label.Small(
                    modifier = Modifier.fillMaxWidth(),
                    text = episode.duration.minToMs.timeDescription(),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary
                )


            }

        }

        Text.Body.Medium(
            text = episode.description,
            color = MaterialTheme.colorScheme.onBackground,
        )

    }

    if (showMenu) {
        EpisodeDropDownMenu(
            episode = episode,
            onDismissRequest = { showMenu = false },
            sendIntent = sendIntent
        )
    }

}

@Composable
fun EpisodeImage(
    modifier: Modifier,
    episode: Episode
) {

    Box(
        modifier = modifier
            .clip(Ui.Shape.Corner.Small)
            .aspectRatio(16f / 9f),
        contentAlignment = Alignment.BottomCenter,
        content = {

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (episode.status == Status.WATCHED) it.grayScale() else it },
                url = Constants.TMDB.IMAGE + episode.imagePath,
                contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
            )

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.BottomCenter),
                visible = episode.status == Status.IS_WATCHING
            ) {
                ProgressBar(
                    modifier = Modifier
                        .height(8.dp)
                        .fillMaxWidth(),
                    media = episode
                )
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.Center),
                visible = episode.status == Status.WATCHED
            ) {
                Box(
                    modifier = Modifier
                        .clip(Ui.Shape.Corner.Small)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .height(32.dp)
                        .widthIn(min = 40.dp)
                        .padding(horizontal = Ui.Space.SMALL),
                    contentAlignment = Alignment.Center
                ) {
                    Text.Label.Medium(
                        color = MaterialTheme.colorScheme.onTertiary,
                        text = stringResource(R.string.watched)
                    )
                }
            }

        }
    )

}

@Composable
fun EpisodeDropDownMenu(
    episode: Episode,
    onDismissRequest: () -> Unit,
    sendIntent: (MediaIntent) -> Unit
) {

    val text = when (episode.status) {
        Status.WATCHED -> stringResource(R.string.rewatch)
        Status.IS_WATCHING -> stringResource(R.string.resume)
        else -> stringResource(R.string.start)
    }

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        content = {

            DropdownMenuItem(
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                onClick = {
                    sendIntent(MediaIntent.PlayMedia(media = episode))
                    onDismissRequest()
                },
                text = {
                    Text.Body.Medium(text = text)
                },
                leadingIcon = {
                    Icon(imageVector = if (episode.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow, contentDescription = null)
                },
            )

            DropdownMenuItem(
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    leadingIconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
                onClick = {
                    sendIntent(MediaIntent.ChangeWatchStatus(media = episode))
                    onDismissRequest()
                },
                text = {
                    Text.Body.Medium(stringResource(if (episode.status == Status.WATCHED) R.string.mark_as_not_watched else R.string.mark_as_watched))
                },
                leadingIcon = {
                    if (episode.status == Status.WATCHED)
                        Icon(painter = painterResource(R.drawable.ic_visibility), contentDescription = null)
                    else
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                },
            )

        }
    )

}

@Preview
@Composable
fun EpisodeItem_Preview() {
    FluxTheme {
        EpisodeItem(
            episode = MediaMockups.episode1,
            sendIntent = {}
        )
    }
}

@Preview
@Composable
fun EpisodeItemWatching_Preview() {
    FluxTheme {
        EpisodeItem(
            episode = MediaMockups.episode1.copy(
                status = Status.IS_WATCHING,
                currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
            ),
            sendIntent = {}
        )
    }
}

@Preview
@Composable
fun EpisodeItemWatched_Preview() {
    FluxTheme {
        EpisodeItem(
            episode = MediaMockups.episode1.copy(
                status = Status.WATCHED,
                currentTime = MediaMockups.episode1.duration.minToMs,
            ),
            sendIntent = {}
        )
    }
}
