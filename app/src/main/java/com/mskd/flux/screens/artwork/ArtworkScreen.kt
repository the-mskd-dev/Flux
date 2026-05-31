package com.mskd.flux.screens.artwork

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.screens.artwork.composables.ArtworkContentLarge
import com.mskd.flux.screens.artwork.composables.ArtworkContentRegular
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxDialog
import com.mskd.flux.ui.component.FluxDropDownMenu
import com.mskd.flux.ui.component.FluxDropDownMenuItem
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.ResetProgressDialog
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.WebLink
import com.mskd.flux.utils.rememberExternalPlayerLauncher

@Composable
fun ArtworkScreen(
    artworkId: Long,
    season: Int?,
    colorScheme: ColorScheme,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: ArtworkViewModel = hiltViewModel<ArtworkViewModel, ArtworkViewModel.Factory>(
        creationCallback = { factory -> factory.create(artworkId = artworkId, season = season) }
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
                is ArtworkEvent.OpenUrlInfo -> WebLink.openPage(context = context, url = event.url)
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
        targetState = uiState.state::class,
        label = "MediaScreenAnimation"
    ) { stateClass ->

        when (stateClass) {
            State.Loading::class -> LoadingScreen()
            State.Error::class -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(ArtworkIntent.OnBackTap) }
                )
            }
            State.Content::class -> {
                val content = (uiState.state as State.Content<ArtworkContent>).content
                MaterialTheme(colorScheme = colorScheme) {
                    ArtworkScreenContent(
                        fullArtwork = content.fullArtwork,
                        selectedMedia = content.selectedMedia,
                        selectedSeason = content.selectedSeason,
                        dialog = content.dialog,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }

        }

    }

}

@Composable
fun ArtworkScreenContent(
    fullArtwork: FullArtwork,
    selectedMedia: Media,
    selectedSeason: Int?,
    dialog: ArtworkDialog?,
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

    FluxScaffold(
        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
        title = when {
            isLargeScreen -> null
            fullArtwork is FullArtwork.FullMovie -> fullArtwork.artwork.title
            fullArtwork is FullArtwork.FullShow -> fullArtwork.seasons.find { it.season == selectedSeason }?.title ?: fullArtwork.artwork.title
            else -> null
        },
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = if (fullArtwork.contentType == ContentType.SHOW) MaterialTheme.colorScheme.background else Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
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
        onBackTap = { sendIntent(ArtworkIntent.OnBackTap) },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->

        if (isLargeScreen) {
            ArtworkContentLarge(
                fullArtwork = fullArtwork,
                currentMedia = selectedMedia,
                currentSeason = selectedSeason,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        } else {
            ArtworkContentRegular(
                fullArtwork = fullArtwork,
                currentMedia = selectedMedia,
                currentSeason = selectedSeason,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        }

    }

    if (dialog is ArtworkDialog.EpisodeStatusConfirmation) {
        FluxDialog(
            content = {
                Text.Body.Large(text = stringResource(R.string.mark_previous_episodes_as_watched))
            },
            onDismiss = { sendIntent(ArtworkIntent.CloseDialog) },
            onValidate = { sendIntent(ArtworkIntent.MarkPreviousEpisodesAsWatched) }
        )
    }

    if (dialog is ArtworkDialog.ResetProgressConfirmation) {
        ResetProgressDialog(
            onValidate = { sendIntent(ArtworkIntent.ResetProgress) },
            onDismiss = { sendIntent(ArtworkIntent.CloseDialog) }
        )
    }

}
@Composable
fun ArtworkDropDownMenu(
    onDismissRequest: () -> Unit,
    sendIntent: (ArtworkIntent) -> Unit
) {

    FluxDropDownMenu(
        onDismissRequest = onDismissRequest,
        items = listOf(
            FluxDropDownMenuItem(
                text = stringResource(R.string.more_info),
                onClick = {
                    sendIntent(ArtworkIntent.OpenArtworkInfo)
                    onDismissRequest()
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = stringResource(R.string.more_info)) },
            ),
            FluxDropDownMenuItem(
                text = stringResource(R.string.reset_progress),
                onClick = {
                    sendIntent(ArtworkIntent.ShowResetProgressDialog)
                    onDismissRequest()
                },
                leadingIcon = { Icon(painter = painterResource(R.drawable.ic_eraser), contentDescription = stringResource(R.string.reset_progress)) },
            )
        )
    )

}

@FluxPreview
@Composable
fun ArtworkScreenContent_Preview() {
    AppTheme {
        ArtworkScreenContent(
            fullArtwork = MediaMockups.fullShow,
            selectedMedia = MediaMockups.episode1,
            selectedSeason = MediaMockups.episode1.season,
            dialog = null,
            sendIntent = {}
        )
    }
}