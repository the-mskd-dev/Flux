package com.mskd.flux.screens.artwork.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.screens.artwork.composables.common.ArtworkButtons
import com.mskd.flux.screens.artwork.composables.common.ArtworkDescription
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.screens.artwork.composables.episodes.EpisodeItem
import com.mskd.flux.screens.artwork.composables.episodes.SeasonsTabs
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkContentRegular(
    artwork: Artwork,
    media: Media,
    seasons: List<Season>,
    episodes: List<Episode>,
    currentSeason: Int,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    val state = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        item {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            ) {

                ArtworkImage(
                    modifier = Modifier.aspectRatio(6f / 5f),
                    artwork = artwork,
                    sendIntent = sendIntent
                )

                ArtworkButtons(
                    media = media,
                    sendIntent = sendIntent
                )

                ArtworkDescription(media = media)

            }

        }

        if (episodes.isNotEmpty()) {

            item {

                Column(
                    modifier = Modifier.padding(top = Ui.Space.LARGE),
                    verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
                ) {

                    Text.Title.Large(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Ui.Space.MEDIUM),
                        text = stringResource(R.string.episode_list),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    SeasonsTabs(
                        selectedSeason = currentSeason,
                        seasons = episodes.map { it.season }.distinct(),
                        onSeasonTap = { sendIntent(ArtworkIntent.SelectSeason(it)) }
                    )

                    Text.Body.Large(
                        text = seasons.find { it.season == currentSeason }?.description
                    )

                }

            }


            itemsIndexed(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                key = { _, e -> e.id }
            ) { i, episode ->

                if (i != 0) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM))
                }

                EpisodeItem(
                    modifier = Modifier.animateItem(),
                    episode = episode,
                    sendIntent = sendIntent
                )

            }

        }

        item {
            Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateBottomPadding()))
        }

    }

}

@FluxPreview
@Composable
fun ArtworkContentMovie_Preview() {
    AppTheme {
        ArtworkContentRegular(
            artwork = MediaMockups.movieArtwork,
            media = MediaMockups.movie,
            seasons = emptyList(),
            episodes = emptyList(),
            currentSeason = -1,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun ArtworkContentShow_Preview() {
    AppTheme {
        ArtworkContentRegular(
            artwork = MediaMockups.showArtwork,
            media = MediaMockups.episode1,
            seasons = MediaMockups.seasons,
            episodes = MediaMockups.episodesWithStatus,
            currentSeason = 1,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}