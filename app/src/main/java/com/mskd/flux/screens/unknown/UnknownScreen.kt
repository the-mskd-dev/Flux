package com.mskd.flux.screens.unknown

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.R
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.navigation.Route
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.LoadingScreen

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UnknownScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: UnknownViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                UnknownEvent.BackToPreviousScreen -> onBack()
                is UnknownEvent.PlayMedia -> navigate(Route.Player(mediaId = event.mediaId))
            }
        }
    }

    Crossfade(targetState = uiState.screen) { screen ->

        when (screen) {
            ScreenState.LOADING -> LoadingScreen()
            ScreenState.ERROR -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(UnknownIntent.OnBackTap) }
                )
            }
            ScreenState.CONTENT -> {
                UnknownScreenContent(
                    medias = uiState.medias,
                    sendIntent = viewModel::handleIntent
                )
            }
        }

    }

}

@Composable
fun UnknownScreenContent(
    medias: List<Episode>,
    sendIntent: (UnknownIntent) -> Unit
) {

}