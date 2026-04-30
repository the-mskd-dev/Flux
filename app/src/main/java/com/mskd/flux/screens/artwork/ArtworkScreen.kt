package com.mskd.flux.screens.artwork

import android.content.ActivityNotFoundException
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.R
import com.mskd.flux.model.ScreenState
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.screens.artwork.composables.ArtworkContentLarge
import com.mskd.flux.screens.artwork.composables.ArtworkContentRegular
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxDialog
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.Text
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.WebLink

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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ArtworkEvent.BackToPreviousScreen -> onBack()
                is ArtworkEvent.PlayMedia -> navigate(Player(mediaId = event.mediaId))
                is ArtworkEvent.OpenArtworkInfo -> WebLink.openPage(context = context, url = event.artwork.infoUrl)
                is ArtworkEvent.OpenEpisodeInfo -> WebLink.openPage(context = context, url = event.episode.infoUrl)
                is ArtworkEvent.LaunchExternalPlayer -> {

                    try {
                        val intent = ExternalPlayer.createIntent(media = event.media, context = context)
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Log.e("ArtworkScreen", "No player founded", e)
                        viewModel.handleIntent(ArtworkIntent.PlayMedia(media = event.media, forceInternal = true))
                    }

                }
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
                        hideProgress = uiState.useExternalPlayer,
                        sendIntent = viewModel::handleIntent,
                    )
                } else {
                    ArtworkContentRegular(
                        artwork = uiState.artwork,
                        media = uiState.media,
                        episodes = uiState.episodes,
                        currentSeason = uiState.season,
                        hideProgress = uiState.useExternalPlayer,
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