package com.kaem.flux.screens.media.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.screens.media.composables.episodes.MediaEpisodeImage
import com.kaem.flux.screens.media.composables.episodes.MediaEpisodeTitleDesc
import com.kaem.flux.screens.media.composables.episodes.MediaSeasonsTabs
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.grayScale

@Composable
fun MediaEpisodesSheet(
    selectedId: Long?,
    episodes: List<Episode>,
    currentSeason: Int,
    sendIntent: (MediaIntent) -> Unit,
) {

    val state = rememberLazyListState()
    var screenWidth = 0.dp
    val filteredEpisodes = episodes
        .filter { it.season == currentSeason }
        .sortedBy { it.number }

    // Get screen width
    with(LocalDensity.current) {
        screenWidth = LocalWindowInfo.current.containerSize.width.toDp()
    }

    // Scroll to selected episode
    LaunchedEffect(selectedId) {
        val episodeIndex = episodes.indexOfFirst { it.id == selectedId }
        if (episodeIndex >= 0) state.animateScrollToItem(episodeIndex)
    }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
    ) {

        MediaSeasonsTabs(
            selectedSeason = currentSeason,
            seasons = episodes.map { it.season }.distinct(),
            onSeasonTap = { sendIntent(MediaIntent.SelectSeason(it)) }
        )

        LazyRow(
            state = state,
            contentPadding = PaddingValues(all = Ui.Space.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
        ) {

            items(
                items = filteredEpisodes,
                key = { e -> e.id }
            ) { episode ->

                Card(
                    modifier = Modifier.width(screenWidth.times(.8f)),
                    onClick = { sendIntent(MediaIntent.SelectEpisode(episode)) },
                    colors = if (episode.id == selectedId) Ui.Card.selectedCardColors else CardDefaults.cardColors(),
                    content = { EpisodeItemHorizontal(episode = episode) }
                )

            }

        }

    }

}



@Composable
fun EpisodeItemHorizontal(episode: Episode) {

    val episodeModifier = if (episode.status == Status.WATCHED)
        Modifier
            .alpha(.4f)
            .grayScale()
    else
        Modifier

    Column(
        modifier = episodeModifier
            .fillMaxWidth()
            .padding(Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        MediaEpisodeImage(
            episode = episode,
            width = 200.dp
        )

        MediaEpisodeTitleDesc(
            episode = episode,
            fixedLines = true
        )

    }

}

@Preview
@Composable
fun MediaEpisodesSheet_Preview() {
    FluxTheme {
        MediaEpisodesSheet(
            selectedId = MediaMockups.episode2.id,
            episodes = MediaMockups.episodes,
            currentSeason = 1,
            sendIntent = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun EpisodeItemHorizontal_Preview() {
    FluxTheme {
        EpisodeItemHorizontal(
            episode = MediaMockups.episode1,
        )
    }
}
