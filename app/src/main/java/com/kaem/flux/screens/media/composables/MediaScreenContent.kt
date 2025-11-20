package com.kaem.flux.screens.media.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreenContent(
    overview: MediaOverview,
    media: Media?,
    episodes: List<Episode>,
    currentSeason: Int,
    sendIntent: (MediaIntent) -> Unit,
) {

    val state = rememberLazyListState()

    LaunchedEffect(Unit) {
        state.scrollToItem(0)
    }

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

                MediaImage(
                    modifier = Modifier.aspectRatio(6f / 5f),
                    overview = overview,
                    sendIntent = sendIntent
                )

                MediaButtons(
                    media = media,
                    sendIntent = sendIntent
                )

                MediaDescription(media = media)

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
            Spacer(Modifier.navigationBarsPadding())
        }

    }

}

@Preview
@Composable
fun MediaContentMovie_Preview() {
    FluxTheme {
        MediaScreenContent(
            overview = MediaMockups.movieOverview,
            media = MediaMockups.movie,
            episodes = emptyList(),
            currentSeason = -1,
            sendIntent = {}
        )
    }
}

@Preview
@Composable
fun MediaContentShow_Preview() {
    FluxTheme {
        MediaScreenContent(
            overview = MediaMockups.showOverview,
            media = MediaMockups.episode1,
            episodes = MediaMockups.episodes,
            currentSeason = 1,
            sendIntent = {}
        )
    }
}