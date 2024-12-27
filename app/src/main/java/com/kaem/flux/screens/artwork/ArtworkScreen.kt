package com.kaem.flux.screens.artwork

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.screens.player.PlayerScreen
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxElevation
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.inMinutes
import com.kaem.flux.utils.timeDescription
import kotlinx.coroutines.launch
import java.text.DateFormat

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