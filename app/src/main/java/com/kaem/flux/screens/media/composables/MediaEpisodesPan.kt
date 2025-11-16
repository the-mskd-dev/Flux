package com.kaem.flux.screens.media.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.screens.media.composables.episodes.EpisodeItem
import com.kaem.flux.screens.media.composables.episodes.SeasonsTabs
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui

@Composable
fun MediaEpisodesPan(
    episodes: List<Episode>,
    currentSeason: Int,
    sendIntent: (MediaIntent) -> Unit,
) {

    Column(
        modifier = Modifier
            .navigationBarsPadding()
    ) {

        SeasonsTabs(
            selectedSeason = currentSeason,
            seasons = episodes.map { it.season }.distinct(),
            onSeasonTap = { sendIntent(MediaIntent.SelectSeason(it)) }
        )

        LazyColumn {

            items(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                key = { e -> e.id }
            ) { episode ->

                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Ui.Space.MEDIUM)
                    .animateItem()
                ) {

                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        onTap = { sendIntent(MediaIntent.PlayMedia(episode)) }
                    )

                }

            }

        }

    }

}


@Preview
@Composable
fun MediaEpisodesPan_Preview() {
    FluxTheme {
        MediaEpisodesPan(
            episodes = MediaMockups.episodes,
            currentSeason = 1,
            sendIntent = {}
        )
    }
}