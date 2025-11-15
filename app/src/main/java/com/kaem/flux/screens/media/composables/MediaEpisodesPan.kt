package com.kaem.flux.screens.media.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.screens.media.composables.episodes.MediaSeasonsTabs
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.extensions.grayScale

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

        MediaSeasonsTabs(
            selectedSeason = currentSeason,
            seasons = episodes.map { it.season }.distinct(),
            onSeasonTap = { sendIntent(MediaIntent.SelectSeason(it)) }
        )

        LazyColumn {

            items(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                //key = { e -> e.id }
            ) { episode ->

                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Ui.Space.MEDIUM)
                    .animateItem()
                ) {

                    EpisodeItemVertical(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        onTap = { sendIntent(MediaIntent.PlayMedia(episode)) }
                    )

                }

            }

        }

    }

}

@Composable
fun EpisodeItemVertical(
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

    ConstraintLayout(
        modifier = episodeModifier
            .clickable { onTap() }
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = Ui.Space.MEDIUM)
    ) {

        val (image, content) = createRefs()
        val startGuideline = createGuidelineFromStart(.3f)

        Box(
            modifier = Modifier
                .clip(Ui.Shape.Corner.Small)
                .aspectRatio(16f / 9f)
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(startGuideline)
                    width = Dimension.fillToConstraints
                },
            contentAlignment = Alignment.BottomCenter,
            content = {

                Image(
                    modifier = Modifier.fillMaxSize(),
                    url = Constants.TMDB.IMAGE + episode.imagePath,
                    contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
                )

                if (episode.status == Status.IS_WATCHING) {
                    ProgressBar(
                        modifier = Modifier.fillMaxWidth(),
                        media = episode
                    )
                }

            }
        )

        Column(
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(image.top)
                    start.linkTo(startGuideline, Ui.Space.MEDIUM)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalAlignment = Alignment.Start
        ) {

            Text.Label.Medium(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.episode, episode.number).uppercase(),
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.primary
            )

            Text.Body.Large(
                modifier = Modifier.fillMaxWidth(),
                text = episode.title,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
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

@Preview(showBackground = true)
@Composable
fun EpisodeItemVertical_Preview() {
    FluxTheme {
        EpisodeItemVertical(
            episode = MediaMockups.episode1,
            onTap = {}
        )
    }
}
