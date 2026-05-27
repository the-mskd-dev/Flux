package com.mskd.flux.screens.show

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.screens.artwork.ArtworkEvent
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.screens.artwork.ArtworkScreenContent
import com.mskd.flux.screens.artwork.ArtworkViewModel
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.WebLink

@Composable
fun ShowScreen(
    artworkId: Long,
    colorScheme: ColorScheme,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: ShowViewModel = hiltViewModel<ShowViewModel, ShowViewModel.Factory>(
        creationCallback = { factory -> factory.create(artworkId) }
    )
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ShowEvent.BackToPreviousScreen -> onBack()
                is ShowEvent.NavigateToSeason -> { TODO() }
            }
        }
    }

    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = uiState.state::class,
        label = "MediaScreenAnimation"
    ) { stateClass ->

        when (stateClass) {
            State.Loading::class -> LoadingScreen()
            State.Error::class -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(ShowIntent.OnBackTap) }
                )
            }
            State.Content::class -> {
                MaterialTheme(colorScheme = colorScheme) {
                    ShowScreenContent(
                        uiState = uiState,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }

        }

    }

}

@Composable
fun ShowScreenContent(
    uiState: ShowUiState,
    sendIntent: (ShowIntent) -> Unit
) {

}