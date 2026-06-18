package com.mskd.flux.ui.component.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.mskd.flux.R
import com.mskd.flux.data.repository.customization.LocalCustomization
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.Status
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.global.ReadMoreButton
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.PortraitPreview
import com.mskd.flux.utils.extensions.minToMs

@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean = false,
    onTap: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit = {},
    dropDownMenu: @Composable ((onDismissRequest: () -> Unit) -> Unit)? = null
) {

    val customization = LocalCustomization.current

    if (customization.largeEpisodeImage) {
        EpisodeItemLarge(
            modifier = modifier,
            episode = episode,
            isSelected = isSelected,
            isExpanded = isExpanded,
            onTap = onTap,
            onReadMoreTap = onReadMoreTap,
            dropDownMenu = dropDownMenu
        )
    } else {
        EpisodeItemSmall(
            modifier = modifier,
            episode = episode,
            isSelected = isSelected,
            isExpanded = isExpanded,
            onTap = onTap,
            onReadMoreTap = onReadMoreTap,
            dropDownMenu = dropDownMenu
        )
    }

}
@Composable
fun EpisodeItemLarge(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean,
    onTap: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit,
    dropDownMenu: @Composable ((onDismissRequest: () -> Unit) -> Unit)? = null
) {

    var isOverflowing by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)

    Column(
        modifier = modifier
            .padding(horizontal = Ui.Space.medium)
            .clip(Ui.Shape.itemCard)
            .background(bgColor)
            .combinedClickable(
                onClick = { onTap(episode) },
                onLongClick = { showMenu = dropDownMenu != null }
            )
            .then(
                if (hasLaidOut) Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) else Modifier
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.small),
    ) {

        MediaThumbnail(
            modifier = Modifier.fillMaxWidth(),
            media = episode,
            hd = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Ui.Space.medium),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.small),
            horizontalAlignment = Alignment.Start
        ) {

            val title = buildString {
                if (episode.number >= 0 && !episode.isUnknown)
                    append("${episode.number}. ")
                append(episode.title)
            }
            Text.Title.Medium(
                modifier = Modifier.fillMaxWidth(),
                text =  title,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                emphasized = true
            )

            if (episode.isUnknown) {
                MediaDetailsVertical(media = episode)
            } else {
                MediaDetailsHorizontal(media = episode)
            }

            Text.Body.Medium(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                text = episode.description,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!isExpanded) {
                        isOverflowing = result.hasVisualOverflow
                        hasLaidOut = true
                    }
                }
            )

            if (isOverflowing || isExpanded) {
                ReadMoreButton(
                    modifier = Modifier.align(Alignment.End),
                    onTap = { onReadMoreTap(!isExpanded) },
                    isExpanded = isExpanded
                )
            }

        }

        if (showMenu)
            dropDownMenu?.invoke { showMenu = false }
    }

}

@Composable
fun EpisodeItemSmall(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean,
    onTap: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit,
    dropDownMenu: @Composable ((onDismissRequest: () -> Unit) -> Unit)? = null
) {

    var isOverflowing by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)

    Column(
        modifier = modifier
            .padding(horizontal = Ui.Space.medium)
            .clip(Ui.Shape.itemCard)
            .background(bgColor)
            .combinedClickable(
                onClick = { onTap(episode) },
                onLongClick = { showMenu = dropDownMenu != null }
            )
            .then(
                if (hasLaidOut) Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                ) else Modifier
            )
            .fillMaxWidth()
            .padding(Ui.Space.medium),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.small),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.small)
        ) {

            MediaThumbnail(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .weight(.4f),
                media = episode,
            )

            Column(
                modifier = Modifier.weight(.6f),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.extraSmall),
                horizontalAlignment = Alignment.Start
            ) {

                val title = buildString {
                    if (episode.number >= 0 && !episode.isUnknown)
                        append("${episode.number}. ")
                    append(episode.title)
                }
                Text.Title.Medium(
                    modifier = Modifier.fillMaxWidth(),
                    text =  title,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    emphasized = true
                )

                MediaDetailsVertical(media = episode)

            }

        }

        Text.Body.Medium(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            text = episode.description,
            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onBackground,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded) {
                    isOverflowing = result.hasVisualOverflow
                    hasLaidOut = true
                }
            }
        )

        if (isOverflowing || isExpanded) {
            ReadMoreButton(
                modifier = Modifier.align(Alignment.End),
                onTap = { onReadMoreTap(!isExpanded) },
                isExpanded = isExpanded
            )
        }

        if (showMenu) {
            dropDownMenu?.invoke { showMenu = false }
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

    FluxDropDownMenu(
        onDismissRequest = onDismissRequest,
        items = listOf(
            FluxDropDownMenuItem(
                text = text,
                onClick = {
                    sendIntent(ArtworkIntent.PlayMedia(media = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(imageVector = if (episode.status == Status.WATCHED) Icons.Default.Refresh else Icons.Default.PlayArrow, contentDescription = null)
                },
            ),
            FluxDropDownMenuItem(
                text = if (episode.status == Status.WATCHED) stringResource(R.string.mark_as_not_watched) else stringResource(R.string.mark_as_watched),
                onClick = {
                    sendIntent(ArtworkIntent.ChangeWatchStatus(media = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    if (episode.status == Status.WATCHED)
                        Icon(painter = painterResource(R.drawable.ic_visibility), contentDescription = null)
                    else
                        Icon(imageVector = Icons.Default.Done, contentDescription = null)
                },
            ),
            FluxDropDownMenuItem(
                text = stringResource(R.string.more_info),
                onClick = {
                    sendIntent(ArtworkIntent.OpenEpisodeInfo(episode = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                },
            )
        )
    )

}

@PortraitPreview
@Composable
fun EpisodeItem_Preview() {
    AppThemePreview {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.medium)
        ) {
            EpisodeItemLarge(
                episode = MediaMockups.episode1,
                isSelected = false,
                isExpanded = false,
                onReadMoreTap = {},
                onTap = {}
            )
            EpisodeItemSmall(
                episode = MediaMockups.episode1,
                isSelected = false,
                isExpanded = false,
                onReadMoreTap = {},
                onTap = {}
            )
        }
    }
}

@PortraitPreview
@Composable
fun EpisodeItemWatching_Preview() {
    AppThemePreview {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.medium)
        ) {
            EpisodeItemLarge(
                episode = MediaMockups.episode1.copy(
                    status = Status.IS_WATCHING,
                    currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
                ),
                isSelected = true,
                isExpanded = false,
                onReadMoreTap = {},
                onTap = {}
            )
            EpisodeItemSmall(
                episode = MediaMockups.episode1.copy(
                    status = Status.IS_WATCHING,
                    currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
                ),
                isSelected = true,
                isExpanded = false,
                onReadMoreTap = {},
                onTap = {}
            )
        }
    }
}

@PortraitPreview
@Composable
fun EpisodeItemWatched_Preview() {
    AppThemePreview {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.medium)
        ) {
            EpisodeItemLarge(
                episode = MediaMockups.episode1.copy(
                    status = Status.WATCHED,
                    currentTime = MediaMockups.episode1.duration.minToMs,
                ),
                isSelected = false,
                isExpanded = false,
                onReadMoreTap = {},
                onTap = {}
            )
            EpisodeItemSmall(
                episode = MediaMockups.episode1.copy(
                    status = Status.WATCHED,
                    currentTime = MediaMockups.episode1.duration.minToMs,
                ),
                isSelected = false,
                isExpanded = false,
                onReadMoreTap = {},
                onTap = {}
            )
        }
    }
}