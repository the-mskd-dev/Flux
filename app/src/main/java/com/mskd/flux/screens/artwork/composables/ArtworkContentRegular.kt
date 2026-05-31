package com.mskd.flux.screens.artwork.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.mskd.flux.screens.artwork.composables.common.ArtworkDescriptionsPager
import com.mskd.flux.screens.artwork.composables.common.ArtworkHeader
import com.mskd.flux.screens.artwork.composables.episodes.EpisodeItem
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.PortraitPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkContentRegular(
    fullArtwork: FullArtwork,
    currentMedia: Media,
    currentSeason: Int?,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {

            ArtworkHeader(
                modifier = Modifier.fillMaxWidth(),
                fullArtwork = fullArtwork,
                currentMedia = currentMedia,
                title = fullArtwork.artwork.title,
                sendIntent = sendIntent
            )

        }

        item {
            Spacer(modifier = Modifier.height(Ui.Space.LARGE))
        }

        item {

            ArtworkDescriptionsPager(
                fullArtwork = fullArtwork,
                currentMedia = currentMedia
            )

        }

        item {
            Spacer(modifier = Modifier.height(Ui.Space.LARGE))
        }

        (fullArtwork as? FullArtwork.FullShow)?.let { show ->

            val episodes = show.episodes.filter { it.season == currentSeason }

            if (episodes.isNotEmpty()) {

                item {

                    Column(verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)) {

                        Text.Title.Large(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Ui.Space.MEDIUM),
                            text = stringResource(R.string.episodes),
                            emphasized = true,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                    }

                }

                item {
                    Spacer(modifier = Modifier.height(Ui.Space.MEDIUM))
                }

                items(
                    items = episodes.sortedBy { it.number },
                    key = { e -> e.id to e.currentTime }
                ) { episode ->

                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        isSelected = episode.id == currentMedia.mediaId,
                        sendIntent = sendIntent
                    )

                    Spacer(modifier = Modifier.height(Ui.Space.SMALL))

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
            currentSeason = null,
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