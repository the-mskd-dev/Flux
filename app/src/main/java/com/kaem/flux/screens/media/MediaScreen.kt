package com.kaem.flux.screens.media

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.window.core.layout.WindowSizeClass
import com.kaem.flux.R
import com.kaem.flux.model.ScreenState
import com.kaem.flux.screens.media.composables.MediaScreenContent
import com.kaem.flux.screens.media.composables.MediaScreenContentLarge
import com.kaem.flux.screens.player.PlayerScreen
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.FluxDialog
import com.kaem.flux.ui.component.LoadingScreen
import com.kaem.flux.ui.component.Text

@Composable
fun MediaScreen(
    onBack: () -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLargeScreen = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

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

                Crossfade(
                    modifier = Modifier.fillMaxSize(),
                    targetState = uiState.showPlayer,
                    label = "MediaScreenAnimation"
                ) { showPlayer ->
                    if (showPlayer) {

                        PlayerScreen(
                            media = uiState.media,
                            backward = viewModel.backwardValue,
                            forward = viewModel.forwardValue,
                            subtitlesLanguage = viewModel.subtitlesLanguage,
                            sendIntent = viewModel::handleIntent,
                        )

                    } else {

                        if (isLargeScreen) {
                            MediaScreenContentLarge(
                                overview = uiState.overview,
                                media = uiState.media,
                                episodes = uiState.episodes,
                                currentSeason = uiState.season,
                                sendIntent = viewModel::handleIntent,
                            )
                        } else {
                            MediaScreenContent(
                                overview = uiState.overview,
                                media = uiState.media,
                                episodes = uiState.episodes,
                                currentSeason = uiState.season,
                                sendIntent = viewModel::handleIntent,
                            )
                        }

                    }
                }

            }

        }

    }

    if (uiState.showStatusDialog) {
        FluxDialog(
            content = {
                Text.Body.Large(text = stringResource(R.string.mark_previous_episodes_as_watched))
            },
            onDismiss = { viewModel.handleIntent(MediaIntent.CloseEpisodesStatusDialog) },
            onValidate = { viewModel.handleIntent(MediaIntent.MarkPreviousEpisodesAsWatched) }
        )
    }

}