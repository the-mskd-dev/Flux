package com.mskd.flux.ui.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.components.FixedChip
import com.mskd.flux.ui.components.ReadMoreButton
import com.mskd.flux.ui.blurContent
import com.mskd.flux.ui.blurText
import com.mskd.flux.ui.grayScale
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.minToMs
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.content_unavailable
import flux.shared.generated.resources.spoiler_hidden
import flux.shared.generated.resources.spoiler_reveal
import org.jetbrains.compose.resources.stringResource

@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean = false,
    isBlurred: Boolean = false,
    isSpoiler: Boolean = false,
    onClick: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit = {},
    onToggleSpoiler: () -> Unit = {},
    dropDownMenu: @Composable ((isVisible: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        if (FluxUI.episodes.large) {
            EpisodeItemLarge(
                episode = episode,
                isSelected = isSelected,
                isExpanded = isExpanded,
                isBlurred = isBlurred,
                isSpoiler = isSpoiler,
                onClick = onClick,
                onReadMoreTap = onReadMoreTap,
                onToggleSpoiler = onToggleSpoiler,
                dropDownMenu = dropDownMenu
            )
        } else {
            EpisodeItemSmall(
                episode = episode,
                isSelected = isSelected,
                isExpanded = isExpanded,
                isBlurred = isBlurred,
                isSpoiler = isSpoiler,
                onClick = onClick,
                onReadMoreTap = onReadMoreTap,
                onToggleSpoiler = onToggleSpoiler,
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
/**
 * Tapping the description of a spoiler reveals or hides it instead of playing the episode. A hidden
 * description is announced as hidden, with a "reveal" action, rather than read out.
 */
@Composable
fun Modifier.spoilerToggle(
    isSpoiler: Boolean,
    isBlurred: Boolean,
    onToggle: () -> Unit,
): Modifier {
    if (!isSpoiler) return this
    val hiddenLabel = stringResource(Res.string.spoiler_hidden)
    val revealLabel = stringResource(Res.string.spoiler_reveal)
    return this
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onToggle
        )
        .then(
            if (isBlurred) Modifier.clearAndSetSemantics {
                contentDescription = hiddenLabel
                onClick(label = revealLabel) { onToggle(); true }
            } else Modifier
        )
}

@Composable
fun EpisodeItemLarge(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean,
    isBlurred: Boolean = false,
    isSpoiler: Boolean = false,
    onClick: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit,
    onToggleSpoiler: () -> Unit = {},
    dropDownMenu: @Composable ((isVisible: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null
) {

    var isOverflowing by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)

    Column(
        modifier = modifier
            .clip(FluxUI.shapes.corners)
            .let { if (episode.isAvailable) it else it.grayScale() }
            .background(bgColor)
            .combinedClickable(
                onClick = { onClick(episode) },
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
            modifier = Modifier
                .fillMaxWidth()
                .blurContent(blurred = isBlurred),
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
            Text.Card.Title(
                modifier = Modifier.fillMaxWidth(),
                text =  title,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
            )

            if (episode.isUnknown) {
                MediaDetailsVertical(media = episode)
            } else {
                MediaDetailsHorizontal(media = episode)
            }

            Text.Card.Body(
                modifier = Modifier
                    .fillMaxWidth()
                    .spoilerToggle(isSpoiler = isSpoiler, isBlurred = isBlurred, onToggle = onToggleSpoiler)
                    .animateContentSize()
                    .blurText(blurred = isBlurred),
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
                    onClick = { onReadMoreTap(!isExpanded) },
                    isExpanded = isExpanded
                )
            }

        }

        dropDownMenu?.invoke(
            showMenu,
            { showMenu = false }
        )
    }

}

@Composable
fun EpisodeItemSmall(
    modifier: Modifier = Modifier,
    episode: Episode,
    isSelected: Boolean,
    isExpanded: Boolean,
    isBlurred: Boolean = false,
    isSpoiler: Boolean = false,
    onClick: (Episode) -> Unit,
    onReadMoreTap: (Boolean) -> Unit,
    onToggleSpoiler: () -> Unit = {},
    dropDownMenu: @Composable ((isVisible: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null
) {

    var isOverflowing by remember { mutableStateOf(false) }
    var hasLaidOut by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)

    Column(
        modifier = modifier
            .clip(FluxUI.shapes.corners)
            .let { if (episode.isAvailable) it else it.grayScale() }
            .background(bgColor)
            .combinedClickable(
                onClick = { onClick(episode) },
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
                    .blurContent(blurred = isBlurred)
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
                Text.Card.Title(
                    modifier = Modifier.fillMaxWidth(),
                    text =  title,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                )

                MediaDetailsVertical(media = episode)

            }

        }

        Text.Card.Body(
            modifier = Modifier
                .fillMaxWidth()
                .spoilerToggle(isSpoiler = isSpoiler, isBlurred = isBlurred, onToggle = onToggleSpoiler)
                .animateContentSize()
                .blurText(blurred = isBlurred),
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
                onClick = { onReadMoreTap(!isExpanded) },
                isExpanded = isExpanded
            )
        }

        dropDownMenu?.invoke(
            showMenu,
            { showMenu = false }
        )

    }

}

@PortraitPreview
@Composable
fun EpisodeItem_Preview() {
    FluxThemePreview {
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
                onClick = {}
            )
            EpisodeItemSmall(
                episode = MediaMockups.episode1,
                isSelected = false,
                isExpanded = false,
                onReadMoreTap = {},
                onClick = {}
            )
        }
    }
}

@PortraitPreview
@Composable
fun EpisodeItemWatching_Preview() {
    FluxThemePreview {
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
                onClick = {}
            )
            EpisodeItemSmall(
                episode = MediaMockups.episode1.copy(
                    status = Status.IS_WATCHING,
                    currentTime = (MediaMockups.episode1.duration.minToMs / 2f).toLong(),
                ),
                isSelected = true,
                isExpanded = false,
                onReadMoreTap = {},
                onClick = {}
            )
        }
    }
}

@PortraitPreview
@Composable
fun EpisodeItemWatched_Preview() {
    FluxThemePreview {
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
                onClick = {}
            )
            EpisodeItemSmall(
                episode = MediaMockups.episode1.copy(
                    status = Status.WATCHED,
                    currentTime = MediaMockups.episode1.duration.minToMs,
                ),
                isSelected = false,
                isExpanded = false,
                onReadMoreTap = {},
                onClick = {}
            )
        }
    }
}