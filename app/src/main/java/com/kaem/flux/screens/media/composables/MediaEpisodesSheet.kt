package com.kaem.flux.screens.media.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
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
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            items(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                //key = { e -> e.id }
            ) { episode ->

                Column(modifier = Modifier
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
fun MediaSeasonsTabs(
    selectedSeason: Int,
    seasons: List<Int>,
    onSeasonTap: (Int) -> Unit
) {

    LazyRow(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        items(items = seasons.sorted(), key = { it }) { season ->

            val isSelected = selectedSeason == season

            FilterChip(
                onClick = { onSeasonTap(season) },
                label = {
                    Text.Label.Medium(
                        text = stringResource(id = R.string.season, season).uppercase(),
                    )
                },
                selected = isSelected,
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Selected icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else { null },
            )

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
            .padding(vertical = Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        Box(
            modifier = Modifier
                .clip(Ui.Shape.Corner.Small)
                .width(200.dp)
                .aspectRatio(16f / 9f),
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

        Text.Label.Small(
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
            emphasized = true
        )

        Text.Body.Medium(
            modifier = Modifier.fillMaxWidth(),
            text = episode.description,
            textAlign = TextAlign.Start,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
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
