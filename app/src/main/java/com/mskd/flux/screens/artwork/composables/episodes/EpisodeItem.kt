package com.mskd.flux.screens.artwork.composables.episodes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.MediaThumbnail
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.formattedText
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription

@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode,
    sendIntent: (ArtworkIntent) -> Unit
) {

    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .combinedClickable(
                onClick = { sendIntent(ArtworkIntent.PlayMedia(episode)) },
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

            MediaThumbnail(
                modifier = Modifier.weight(.4f),
                media = episode,
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

        if (showMenu) {
            EpisodeDropDownMenu(
                episode = episode,
                onDismissRequest = { showMenu = false },
                sendIntent = sendIntent
            )
        }

    }

}

@Composable
fun EpisodeDropDownMenu(
    episode: Episode,
    onDismissRequest: () -> Unit,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val text = when (episode.status) {
        Status.WATCHED -> stringResource(R.string.rewatch)
        Status.IS_WATCHING -> stringResource(R.string.resume)
        else -> stringResource(R.string.play)
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
                    sendIntent(ArtworkIntent.PlayMedia(media = episode))
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
                    sendIntent(ArtworkIntent.ChangeWatchStatus(media = episode))
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

@FluxPreview
@Composable
fun EpisodeItem_Preview() {
    AppTheme {
        EpisodeItem(
            episode = MediaMockups.episode1,
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun EpisodeItemWatching_Preview() {
    AppTheme {
        EpisodeItem(
            episode = MediaMockups.episode1.copy(
                status = Status.IS_WATCHING,
                currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
            ),
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun EpisodeItemWatched_Preview() {
    AppTheme {
        EpisodeItem(
            episode = MediaMockups.episode1.copy(
                status = Status.WATCHED,
                currentTime = MediaMockups.episode1.duration.minToMs,
            ),
            sendIntent = {}
        )
    }
}