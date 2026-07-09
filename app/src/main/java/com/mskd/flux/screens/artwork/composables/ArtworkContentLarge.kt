package com.mskd.flux.screens.artwork.composables

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.artwork.composables.common.ArtworkButtons
import com.mskd.flux.screens.artwork.composables.common.ArtworkDescriptionsPager
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.EpisodeDropDownMenu
import com.mskd.flux.ui.component.media.EpisodeItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.LandscapePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episodes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtworkContentLarge(
    fullArtwork: FullArtwork,
    selectedMedia: Media,
    selectedSeason: Int?,
    expandedEpisodeId: Long?,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.weight(.5f),) {

            ArtworkImage(
                modifier = Modifier.fillMaxSize(),
                fullArtwork = fullArtwork,
                currentMedia = selectedMedia,
                selectedSeason = selectedSeason,
                orientation = Orientation.Horizontal,
            )

        }

        LazyColumn(
            modifier = Modifier.weight(.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item {

                Text.Display.Small(
                    modifier = Modifier
                        .padding(FluxUI.Space.medium)
                        .wrapContentWidth(),
                    text = when (fullArtwork) {
                        is FullArtwork.FullMovie -> fullArtwork.artwork.title
                        is FullArtwork.FullShow -> fullArtwork.seasons.find { it.season == selectedSeason }?.title ?: fullArtwork.artwork.title
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    emphasized = true
                )

            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item {

                ArtworkButtons(
                    media = selectedMedia,
                    sendIntent = sendIntent
                )

            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item {

                ArtworkDescriptionsPager(
                    fullArtwork = fullArtwork,
                    season = selectedSeason,
                    currentMedia = selectedMedia
                )

            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            (fullArtwork as? FullArtwork.FullShow)?.let { show ->

                val episodes = show.episodes.filter { it.season == selectedSeason }

                if (episodes.isNotEmpty()) {

                    item {

                        Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)) {

                            Text.Title.Large(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = FluxUI.Space.medium),
                                text = stringResource(Res.string.episodes),
                                emphasized = true,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                        }

                    }

                    item {
                        Spacer(modifier = Modifier.height(FluxUI.Space.medium))
                    }

                    items(
                        items = episodes.sortedBy { it.number },
                        key = { e -> e.id to e.currentTime}
                    ) { episode ->

                        EpisodeItem(
                            modifier = Modifier.animateItem(),
                            episode = episode,
                            isSelected = episode.id == selectedMedia.mediaId,
                            isExpanded = episode.id == expandedEpisodeId,
                            onTap = { sendIntent(ArtworkIntent.PlayMedia(media = episode)) },
                            onReadMoreTap = {
                                if (it) {
                                    sendIntent(ArtworkIntent.ExpandEpisodeDescription(episode = episode))
                                } else {
                                    sendIntent(ArtworkIntent.CollapseEpisodeDescription)
                                }
                            },
                            dropDownMenu = { onDismissRequest ->
                                EpisodeDropDownMenu(
                                    episode = episode,
                                    onDismissRequest = onDismissRequest,
                                    sendIntent = sendIntent
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(FluxUI.Space.small))

                    }

                }

            }

            item {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateBottomPadding()))
            }

        }

    }

}


@LandscapePreview
@Composable
fun ArtworkContentLargeMovie_Preview() {
    FluxTheme {
        ArtworkContentLarge(
            fullArtwork = MediaMockups.fullMovie,
            selectedMedia = MediaMockups.movie,
            selectedSeason = null,
            expandedEpisodeId = null,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}

@LandscapePreview
@Composable
fun ArtworkContentLargeShow_Preview() {
    FluxTheme {
        ArtworkContentLarge(
            fullArtwork = MediaMockups.fullShow,
            selectedMedia = MediaMockups.episode1,
            selectedSeason = 1,
            expandedEpisodeId = null,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}