package com.mskd.flux.screens.artwork.composables

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mskd.flux.utils.LandscapePreview

@Composable
fun ArtworkContentLarge(
    fullArtwork: FullArtwork,
    currentMedia: Media,
    currentSeason: Int,
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
                sendIntent = sendIntent
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

                ArtworkButtons(
                    media = currentMedia,
                    sendIntent = sendIntent
                )

            }

            item {

                ArtworkDescription(
                    fullArtwork = fullArtwork,
                    currentMedia = currentMedia
                )

            }

            (fullArtwork as? FullArtwork.FullShow)?.let { show ->

                if (show.episodes.isNotEmpty()) {

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
                                seasons = show.seasons,
                                currentSeason = currentSeason,
                                sendIntent = sendIntent
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

}


@LandscapePreview
@Composable
fun ArtworkContentLargeMovie_Preview() {
    AppTheme {
        ArtworkContentLarge(
            fullArtwork = MediaMockups.fullMovie,
            currentMedia = MediaMockups.movie,
            currentSeason = -1,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}

@LandscapePreview
@Composable
fun ArtworkContentLargeShow_Preview() {
    AppTheme {
        ArtworkContentLarge(
            fullArtwork = MediaMockups.fullShow,
            currentMedia = MediaMockups.episode1,
            currentSeason = 1,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}