package com.kaem.flux.screens.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Media
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.screens.media.composables.EpisodeItem
import com.kaem.flux.screens.media.composables.MediaDescription
import com.kaem.flux.screens.media.composables.MediaHeader
import com.kaem.flux.screens.media.composables.MediaSeasonsTabs
import com.kaem.flux.screens.player.PlayerScreen
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.LoadingScreen
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.launch

@Composable
fun MediaScreen(
    onBack: () -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                MediaEvent.BackToPreviousScreen -> onBack()
            }
        }
    }

    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = uiState.screen,
        label = "MediaScreenAnimation"
    ) { screen ->

        when (screen) {
            ScreenState.LOADING -> LoadingScreen()
            ScreenState.ERROR -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(MediaIntent.OnBackTap) }
                )
            }
            else -> {

                MediaContent(
                    overview = uiState.overview,
                    media = uiState.selectedMedia,
                    episodes = uiState.episodes,
                    currentSeason = uiState.currentSeason,
                    sendIntent = viewModel::handleIntent,
                )

            }

        }

    }

    AnimatedVisibility(uiState.showPlayer) {
        PlayerScreen(
            media = uiState.selectedMedia,
            backward = uiState.backwardValue,
            forward = uiState.forwardValue,
            subtitlesLanguage = uiState.subtitlesLanguage,
            sendIntent = viewModel::handleIntent,
        )
    }

    MediaStatusDialog(
        showStatusDialog = uiState.showStatusDialog,
        onDismiss = { viewModel.handleIntent(MediaIntent.ChangeWatchStatus(false)) },
        onValidate = { viewModel.handleIntent(MediaIntent.ChangeWatchStatusForEpisodeAndPrevious) }
    )

}

@Composable
fun MediaContent(
    overview: MediaOverview,
    media: Media?,
    episodes: List<Episode>,
    currentSeason: Int,
    sendIntent: (MediaIntent) -> Unit,
) {

    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val maxZoom = 1.05f
    val scrollRange = 500

    val firstItemOffset by remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset } }
    val zoom by animateFloatAsState(
        if (firstItemOffset < scrollRange) {
            1f + (maxZoom - 1f) * (firstItemOffset.toFloat() / scrollRange)
        } else {
            maxZoom
        },
        label = "zoomEffect"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = scrollState
    ) {

        item {

            Column(
                modifier = Modifier.padding(bottom = Ui.Space.MEDIUM),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                MediaHeader(
                    overview = overview,
                    media = media,
                    zoom = zoom,
                    sendIntent = sendIntent
                )

                MediaDescription(media = media)

            }

        }

        if (episodes.isNotEmpty()) {

            item {

                MediaSeasonsTabs(
                    selectedSeason = currentSeason,
                    seasons = episodes.map { it.season }.distinct(),
                    onSeasonTap = { sendIntent(MediaIntent.SelectSeason(it)) }
                )

            }

            itemsIndexed(
                items = episodes
                    .filter { it.season == currentSeason }
                    .sortedBy { it.number },
                key = { _, e -> e.id }
            ) { i, episode ->

                Column(modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxSize()
                    .padding(horizontal = Ui.Space.MEDIUM)
                    .animateItem()
                ) {

                    if (i != 0) {
                        HorizontalDivider(
                            modifier = Modifier
                                .alpha(.2f)
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        onTap = {
                            scope.launch {
                                sendIntent(MediaIntent.SelectEpisode(episode))
                                scrollState.animateScrollToItem(0)
                            }
                        }
                    )

                }

            }

        }

        item {

            Spacer(
                Modifier
                    .background(if (episodes.isEmpty()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainer)
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .height(100.dp)
            )

        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaStatusDialog(
    showStatusDialog: Boolean,
    onDismiss: () -> Unit,
    onValidate: () -> Unit
) {

    FluxDialog(
        show = showStatusDialog,
        content = {
            Text.Body.Large(text = stringResource(R.string.mark_previous_episodes_as_watched))
        },
        onDismiss = onDismiss,
        onValidate = onValidate
    )

}

@Preview
@Composable
fun MediaStatusDialog_Preview() {
    FluxTheme {
        MediaStatusDialog(
            showStatusDialog = true,
            onDismiss = {},
            onValidate = {}
        )
    }
}