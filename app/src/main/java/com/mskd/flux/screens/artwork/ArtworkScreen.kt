package com.mskd.flux.screens.artwork

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.R
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.screens.artwork.composables.ArtworkContentLarge
import com.mskd.flux.screens.artwork.composables.ArtworkContentRegular
import com.mskd.flux.screens.artwork.composables.episodes.EpisodeDropDownMenuItem
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxDialog
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.Text
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.WebLink
import com.mskd.flux.utils.rememberExternalPlayerLauncher

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

    val externalPlayerLauncher = rememberExternalPlayerLauncher(
        context = context,
        onProgressResult = { progress ->
            viewModel.handleIntent(ArtworkIntent.OnExternalPlayerResult(progress = progress))
        }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ArtworkEvent.BackToPreviousScreen -> onBack()
                is ArtworkEvent.PlayMedia -> navigate(Player(mediaId = event.mediaId))
                is ArtworkEvent.OpenArtworkInfo -> WebLink.openPage(context = context, url = event.artwork.infoUrl)
                is ArtworkEvent.OpenEpisodeInfo -> WebLink.openPage(context = context, url = event.episode.infoUrl)
                is ArtworkEvent.LaunchExternalPlayer -> {
                    ExternalPlayer.launchPlayer(
                        context = context,
                        media = event.media,
                        launcher = externalPlayerLauncher,
                        onError = { viewModel.handleIntent(ArtworkIntent.PlayMedia(media = event.media, forceInternal = true)) }
                    )
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

@Composable
fun ArtworkDropDownMenu(
    onDismissRequest: () -> Unit,
    sendIntent: (ArtworkIntent) -> Unit
) {

    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        content = {

            ArtworkDropDownMenuItem(
                text = stringResource(R.string.more_info),
                onClick = {
                    sendIntent(ArtworkIntent.OpenArtworkInfo)
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                },
            )

            ArtworkDropDownMenuItem(
                text = "Select metadatas",
                onClick = {
                    //sendIntent(ArtworkIntent.ChangeWatchStatus(media = episode))
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Create, contentDescription = null)
                },
            )

        }
    )

}

@Composable
fun ArtworkDropDownMenuItem(
    text: String,
    onClick: () -> Unit,
    leadingIcon:  @Composable (() -> Unit)?
) {

    DropdownMenuItem(
        colors = MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = onClick,
        text = { Text.Body.Medium(text = text) },
        leadingIcon = leadingIcon,
    )

}