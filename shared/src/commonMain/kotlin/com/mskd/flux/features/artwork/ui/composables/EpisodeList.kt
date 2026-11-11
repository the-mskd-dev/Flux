package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.ui.media.episode.EpisodeDropDownMenu
import com.mskd.flux.ui.media.episode.EpisodeItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episodes
import org.jetbrains.compose.resources.stringResource

/**
 * Episode list of an Artwork page, sorted by number, with the spoiler masking of each Episode.
 * Adds nothing when [episodes] is empty.
 */
fun LazyListScope.episodeList(
    episodes: List<Episode>,
    selectedMediaId: Long,
    expandedEpisodeId: Long?,
    spoilerEpisodeIds: Set<Long>,
    blurredEpisodeIds: Set<Long>,
    titleColor: Color,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    if (episodes.isEmpty()) return

    item {

        Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)) {

            Text.Content.Title(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.episodes),
                color = titleColor
            )

        }

    }

    item {
        Spacer(modifier = Modifier.height(FluxUI.Space.medium))
    }

    items(
        items = episodes.sortedBy { it.number },
        key = { e -> e.id to e.currentTime }
    ) { episode ->

        EpisodeItem(
            modifier = Modifier.animateItem(),
            episode = episode,
            isSelected = episode.id == selectedMediaId,
            onClick = { sendIntent(ArtworkIntent.PlayMedia(media = episode)) },
            isExpanded = episode.id == expandedEpisodeId,
            isBlurred = episode.id in blurredEpisodeIds,
            isSpoiler = episode.id in spoilerEpisodeIds,
            onToggleSpoiler = { sendIntent(ArtworkIntent.ToggleEpisodeSpoiler(episode = episode)) },
            onReadMoreTap = {
                if (it) {
                    sendIntent(ArtworkIntent.ExpandEpisodeDescription(episode = episode))
                } else {
                    sendIntent(ArtworkIntent.CollapseEpisodeDescription)
                }
            },
            dropDownMenu = { isVisible, onDismissRequest ->
                EpisodeDropDownMenu(
                    isVisible = isVisible,
                    episode = episode,
                    onDismissRequest = onDismissRequest,
                    sendIntent = sendIntent
                )
            }
        )

        Spacer(modifier = Modifier.height(FluxUI.Space.small))

    }

}
