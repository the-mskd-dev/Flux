package com.kaem.flux.screens.media.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.screens.media.composables.common.MediaButtons
import com.kaem.flux.screens.media.composables.common.MediaDescription
import com.kaem.flux.screens.media.composables.common.MediaImage
import com.kaem.flux.screens.media.composables.episodes.EpisodeItem
import com.kaem.flux.screens.media.composables.episodes.SeasonsTabs
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui

@Composable
fun MediaScreenContentLarge(
    overview: MediaOverview,
    media: Media,
    episodes: List<Episode>,
    currentSeason: Int,
    sendIntent: (MediaIntent) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        MediaImage(
            modifier = Modifier.weight(.5f),
            overview = overview,
            sendIntent = sendIntent
        )

        LazyColumn(
            modifier = Modifier.weight(.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = Ui.Space.MEDIUM)
        ) {

            item {
                Spacer(Modifier.statusBarsPadding() )
            }

            item {

                MediaButtons(
                    media = media,
                    sendIntent = sendIntent
                )

            }

            item {

                MediaDescription(media = media)

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
                            onSeasonTap = { sendIntent(MediaIntent.SelectSeason(it)) }
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
                Spacer(Modifier.navigationBarsPadding() )
            }

        }

    }

}


@Preview(device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360)
@Composable
fun MediaContentLargeMovie_Preview() {
    FluxTheme {
        MediaScreenContentLarge(
            overview = MediaMockups.movieOverview,
            media = MediaMockups.movie,
            episodes = emptyList(),
            currentSeason = -1,
            sendIntent = {}
        )
    }
}

@Preview(device = Devices.AUTOMOTIVE_1024p, widthDp = 720, heightDp = 360)
@Composable
fun MediaContentLargeShow_Preview() {
    FluxTheme {
        MediaScreenContentLarge(
            overview = MediaMockups.showOverview,
            media = MediaMockups.episode1,
            episodes = MediaMockups.episodes,
            currentSeason = 1,
            sendIntent = {}
        )
    }
}