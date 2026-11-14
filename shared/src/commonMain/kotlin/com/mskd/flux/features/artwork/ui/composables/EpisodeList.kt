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
import com.mskd.flux.ui.media.episode.EpisodeActions
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
    actions: EpisodeActions,
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
            onClick = { actions.onPlay(episode) },
            isExpanded = episode.id == expandedEpisodeId,
            isBlurred = episode.id in blurredEpisodeIds,
            isSpoiler = episode.id in spoilerEpisodeIds,
            onToggleSpoiler = { actions.onToggleSpoiler(episode) },
            onReadMoreTap = { expanded -> if (expanded) actions.onExpand(episode) else actions.onCollapse() },
            dropDownMenu = { isVisible, onDismissRequest ->
                EpisodeDropDownMenu(
                    isVisible = isVisible,
                    episode = episode,
                    onDismissRequest = onDismissRequest,
                    actions = actions,
                )
            }
        )

        Spacer(modifier = Modifier.height(FluxUI.Space.small))

    }

}
