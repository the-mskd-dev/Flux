package com.mskd.flux.screens.artwork

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
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
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.LandscapePreview
import com.mskd.flux.utils.WebLink
import com.mskd.flux.utils.extensions.uppercaseFirstLetter
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
                ArtworkScreenContent(
                    uiState = uiState,
                    sendIntent = viewModel::handleIntent
                )
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

    if (uiState.showEraseProgressDialog) {
        ArtworkEraseProgressDialog(sendIntent = viewModel::handleIntent)
    }


}

@Composable
fun ArtworkScreenContent(
    uiState: ArtworkUiState,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val isLargeScreen = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {

            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text.Headline.Small(
                        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
                        text = uiState.artwork.title,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    IconButton(
                        onClick = { showMenu = true },
                        content = {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "menu button"
                            )
                        }
                    )

                    if (showMenu) {
                        ArtworkDropDownMenu(
                            onDismissRequest = { showMenu = false },
                            sendIntent = sendIntent
                        )
                    }

                },
                navigationIcon = {
                    IconButton(
                        onClick = { sendIntent(ArtworkIntent.OnBackTap) },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "back button"
                            )
                        }
                    )
                },
                scrollBehavior = scrollBehavior
            )

        }
    ) { innerPadding ->

        if (isLargeScreen) {
            ArtworkContentLarge(
                artwork = uiState.artwork,
                media = uiState.media,
                episodes = uiState.episodes,
                currentSeason = uiState.season,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        } else {
            ArtworkContentRegular(
                artwork = uiState.artwork,
                media = uiState.media,
                episodes = uiState.episodes,
                currentSeason = uiState.season,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        }

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
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = stringResource(R.string.more_info))
                },
            )

            ArtworkDropDownMenuItem(
                text = stringResource(R.string.erase_progress),
                onClick = {
                    sendIntent(ArtworkIntent.ShowEraseProgressDialog(show = true))
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.ic_eraser), contentDescription = stringResource(R.string.erase_progress))
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

@FluxPreview
@Composable
fun ArtworkScreenContent_Preview() {
    AppTheme {
        ArtworkScreenContent(
            uiState = ArtworkUiState(
                screen = ScreenState.CONTENT,
                artwork = MediaMockups.showArtwork,
                media = MediaMockups.episode1,
                episodes = MediaMockups.episodes,
                season = MediaMockups.episode1.season
            ),
            sendIntent = {}
        )
    }
}

@Composable
fun ArtworkEraseProgressDialog(
    sendIntent: (ArtworkIntent) -> Unit
) {

    FluxDialog(
        title = stringResource(R.string.erase_progress),
        onDismiss = { sendIntent(ArtworkIntent.ShowEraseProgressDialog(show = false)) },
        onValidateLabel = stringResource(R.string.erase),
        onValidate = { sendIntent(ArtworkIntent.EraseProgress) },
        content = {
            Text.Body.Large(
                text = stringResource(R.string.erase_progress_confirmation)
            )
        }
    )

}