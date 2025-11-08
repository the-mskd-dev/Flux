package com.kaem.flux.screens.media.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.screens.media.MediaIntent
import com.kaem.flux.ui.component.Text
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
                key = { e -> e.id }
            ) { episode ->

                Column(modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxSize()
                    .padding(horizontal = Ui.Space.MEDIUM)
                    .animateItem()
                ) {

                    EpisodeItem(
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
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = Ui.Space.MEDIUM)
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