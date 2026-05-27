package com.mskd.flux.screens.show

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.screens.artwork.ArtworkDropDownMenu
import com.mskd.flux.screens.artwork.ArtworkEvent
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.screens.artwork.ArtworkScreenContent
import com.mskd.flux.screens.artwork.ArtworkViewModel
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.utils.AppThemePreview
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
        targetState = uiState.state,
        label = "MediaScreenAnimation"
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            State.Error -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(ShowIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                MaterialTheme(colorScheme = colorScheme) {
                    ShowScreenContent(
                        fullArtwork = state.content,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }

        }

    }

}

@Composable
fun ShowScreenContent(
    fullArtwork: FullArtwork,
    sendIntent: (ShowIntent) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val titleAlpha by remember {
        derivedStateOf {
            if (scrollBehavior.state.contentOffset < -10f) 1f else 0f
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = titleAlpha,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy
        ),
        label = "TitleAlphaAnimation"
    )

    FluxScaffold(
        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
        title = fullArtwork.artwork.title,
        onBackTap = { sendIntent(ShowIntent.OnBackTap) },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->

        val state = rememberLazyListState()



    }

}

@Composable
fun ShowScreenContent_Preview() {
    AppThemePreview {
        ShowScreenContent(
            fullArtwork = MediaMockups.fullShow,
            sendIntent = {}
        )
    }
}