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
import androidx.compose.foundation.lazy.items
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
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
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
import com.mskd.flux.utils.PortraitPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkContentRegular(
    fullArtwork: FullArtwork,
    currentMedia: Media,
    currentSeason: Int,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    val state = rememberLazyListState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        item {

            ArtworkImage(
                modifier = Modifier.aspectRatio(6f / 5f),
                artwork = fullArtwork.artwork,
                sendIntent = sendIntent
            )

        }

        item {

            ArtworkButtons(
                media = currentMedia,
                sendIntent = sendIntent
            )

        }

        item {

            ArtworkDescription(media = currentMedia)

        }

        (fullArtwork as? FullArtwork.FullShow)?.let { show ->

            if (show.episodes.isNotEmpty()) {

                item {

                    Column(verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)) {

                        Text.Title.Large(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Ui.Space.MEDIUM),
                            text = stringResource(R.string.episode_list),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        SeasonsTabs(
                            selectedSeason = currentSeason,
                            seasons = show.episodes.map { it.season }.distinct(),
                            onSeasonTap = { sendIntent(ArtworkIntent.SelectSeason(it)) }
                        )

                        Text.Body.Large(
                            text = show.seasons.find { it.season == currentSeason }?.description
                        )

                    }

                }

                items(
                    items = show.episodes
                        .filter { it.season == currentSeason }
                        .sortedBy { it.number },
                    key = { e -> e.id }
                ) { episode ->

                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        isSelected = episode.id == currentMedia.mediaId,
                        isLargeScreen = false,
                        sendIntent = sendIntent
                    )

                }

            }

        }

        item {
            Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateBottomPadding()))
        }

    }

}

@PortraitPreview
@Composable
fun ArtworkContentMovie_Preview() {
    AppTheme {
        ArtworkContentRegular(
            fullArtwork = MediaMockups.fullMovie,
            currentMedia = MediaMockups.movie,
            currentSeason = -1,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}

@PortraitPreview
@Composable
fun ArtworkContentShow_Preview() {
    AppTheme {
        ArtworkContentRegular(
            fullArtwork = MediaMockups.fullShow,
            currentMedia = MediaMockups.episode1,
            currentSeason = 1,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}