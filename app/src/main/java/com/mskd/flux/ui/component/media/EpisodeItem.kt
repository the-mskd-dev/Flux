package com.mskd.flux.ui.component.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.FixedChip
import com.mskd.flux.ui.component.global.ReadMoreButton
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.PortraitPreview
import com.mskd.flux.utils.extensions.grayScale
import com.mskd.flux.utils.extensions.minToMs
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.content_unavailable
import org.jetbrains.compose.resources.stringResource

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

    Box(
        contentAlignment = Alignment.Center
    ) {

        if (FluxUI.episodes.large) {
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

        if (!episode.isAvailable) {
            FixedChip(
                text = stringResource(Res.string.content_unavailable),
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                textColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }

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
            .padding(horizontal = FluxUI.Space.medium)
            .clip(FluxUI.shapes.corners)
            .let { if (episode.isAvailable) it else it.grayScale() }
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
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
    ) {

        MediaThumbnail(
            modifier = Modifier.fillMaxWidth(),
            media = episode,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FluxUI.Space.medium),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
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
            .padding(horizontal = FluxUI.Space.medium)
            .clip(FluxUI.shapes.corners)
            .let { if (episode.isAvailable) it else it.grayScale() }
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
            .padding(FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            MediaThumbnail(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .weight(.4f),
                media = episode,
            )

            Column(
                modifier = Modifier.weight(.6f),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
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

@PortraitPreview
@Composable
fun EpisodeItem_Preview() {
    AppThemePreview {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
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