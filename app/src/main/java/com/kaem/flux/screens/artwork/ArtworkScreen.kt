package com.kaem.flux.screens.artwork

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.screens.player.PlayerScreen
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.ui.theme.FluxSpace
import kotlinx.coroutines.launch

@Composable
fun ArtworkScreen(
    onBackButtonTap: () -> Unit,
    viewModel: ArtworkViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = uiState,
        label = "ArtworkScreenAnimation"
    ) { state ->

        when (state.screen) {
            ScreenState.LOADING -> Loader()
            ScreenState.ERROR -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = onBackButtonTap
                )
            }
            else -> {

                ArtworkContent(
                    overview = state.overview,
                    artwork = state.selectedArtwork,
                    episodes = state.episodes,
                    currentSeason = state.currentSeason,
                    onBackButtonTap = onBackButtonTap,
                    onStatusButtonTap = { viewModel.changeWatchStatus() },
                    onSeasonTap = { viewModel.selectSeason(it) },
                    onEpisodeTap = { viewModel.selectArtwork(it) },
                    onPlayerButtonTap = { viewModel.showPlayer(true) }
                )

            }

        }

    }

    AnimatedVisibility(uiState.showPlayer) {
        PlayerScreen(
            artwork = uiState.selectedArtwork,
            onBackButtonTap = { viewModel.showPlayer(false) },
            onTimeSave = { viewModel.saveTime(it) }
        )
    }

}

@Composable
fun ArtworkContent(
    overview: ArtworkOverview,
    artwork: Artwork?,
    episodes: List<Episode>,
    currentSeason: Int,
    onBackButtonTap: () -> Unit,
    onStatusButtonTap: () -> Unit,
    onSeasonTap: (Int) -> Unit,
    onEpisodeTap: (Episode) -> Unit,
    onPlayerButtonTap: () -> Unit
) {

    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp),
        state = scrollState
    ) {

        item {

            Column(
                modifier = Modifier.padding(bottom = FluxSpace.MEDIUM),
                verticalArrangement = Arrangement.spacedBy(FluxSpace.LARGE)
            ) {

                ArtworkHeader(
                    overview = overview,
                    artwork = artwork,
                    onBackButtonTap = onBackButtonTap,
                    onStatusButtonTap = onStatusButtonTap,
                    onPlayerButtonTap = onPlayerButtonTap
                )

                ArtworkDescription(artwork = artwork)

            }

        }

        if (episodes.isNotEmpty()) {

            item {

                ArtworkSeasonsTabs(
                    selectedSeason = currentSeason,
                    seasons = episodes.map { it.season }.distinct(),
                    onSeasonTap = onSeasonTap
                )

            }

            itemsIndexed(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                key = { _, e -> e.id }
            ) { i, episode ->

                EpisodeItem(
                    episode = episode,
                    isFirst = i == 0,
                    onEpisodeTap = {
                        scope.launch {
                            onEpisodeTap(episode)
                            scrollState.animateScrollToItem(0)
                        }
                    }
                )

            }

        }

    }

}