package com.mskd.flux.ui.media.episode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.components.FixedChip
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