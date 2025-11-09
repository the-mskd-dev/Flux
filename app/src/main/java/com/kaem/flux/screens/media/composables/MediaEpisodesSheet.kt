package com.kaem.flux.screens.media.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.screens.media.composables.episodes.MediaEpisodeImage
import com.kaem.flux.screens.media.composables.episodes.MediaEpisodeTitleDesc
import com.kaem.flux.screens.media.composables.episodes.MediaSeasonsTabs
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.grayScale

@Composable
fun MediaEpisodesSheet(
    episodes: List<Episode>,
    currentSeason: Int,
    sendIntent: (MediaIntent) -> Unit,
) {

    var screenWidth = 0.dp
    with(LocalDensity.current) {
        screenWidth = LocalWindowInfo.current.containerSize.width.toDp()
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
            contentPadding = PaddingValues(all = Ui.Space.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
        ) {

            items(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                //key = { e -> e.id }
            ) { episode ->

                Card(
                    modifier = Modifier
                        .width(screenWidth.times(.8f))
                        .animateItem()
                ) {
                    EpisodeItemHorizontal(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        onTap = { sendIntent(MediaIntent.SelectEpisode(episode)) }
                    )
                }

            }

        }

    }

}



@Composable
fun EpisodeItemHorizontal(
    modifier: Modifier = Modifier,
    episode: Episode,
    onTap: () -> Unit
) {

    val episodeModifier = if (episode.status == Status.WATCHED)
        modifier
            .alpha(.4f)
            .grayScale()
    else
        modifier

    Column(
        modifier = episodeModifier
            .clickable { onTap() }
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
            onTap = {}
        )
    }
}
