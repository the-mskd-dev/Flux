package com.mskd.flux.screens.artwork

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.R
import com.mskd.flux.model.ScreenState
import com.mskd.flux.navigation.Route
import com.mskd.flux.screens.artwork.composables.ArtworkContentLarge
import com.mskd.flux.screens.artwork.composables.ArtworkContentRegular
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxDialog
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.Text

@Composable
fun ArtworkScreen(
    artworkId: Long,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: ArtworkViewModel = hiltViewModel<ArtworkViewModel, ArtworkViewModel.Factory>(
        creationCallback = { factory -> factory.create(artworkId) }
    )
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isLargeScreen = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ArtworkEvent.BackToPreviousScreen -> onBack()
                is ArtworkEvent.PlayMedia -> navigate(Route.Player(mediaId = event.mediaId))
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
                    onBackButtonTap = { viewModel.handleIntent(ArtworkIntent.OnBackTap) }
                )
            }
            else -> {

                if (isLargeScreen) {
                    ArtworkContentLarge(
                        artwork = uiState.artwork,
                        media = uiState.media,
                        episodes = uiState.episodes,
                        currentSeason = uiState.season,
                        sendIntent = viewModel::handleIntent,
                    )
                } else {
                    ArtworkContentRegular(
                        artwork = uiState.artwork,
                        media = uiState.media,
                        episodes = uiState.episodes,
                        currentSeason = uiState.season,
                        sendIntent = viewModel::handleIntent,
                    )
                }

            }

        }

    }

    if (uiState.episodePendingConfirmation != null) {
        FluxDialog(
            content = {
                Text.Body.Large(text = stringResource(R.string.mark_previous_episodes_as_watched))
            },
            onDismiss = { viewModel.handleIntent(ArtworkIntent.CloseEpisodesStatusDialog) },
            onValidate = { viewModel.handleIntent(ArtworkIntent.MarkPreviousEpisodesAsWatched) }
        )
    }

}