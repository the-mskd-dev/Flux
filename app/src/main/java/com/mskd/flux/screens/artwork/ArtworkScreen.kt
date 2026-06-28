package com.mskd.flux.screens.artwork

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.State
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Player
import com.mskd.flux.screen.artwork.ArtworkDialog
import com.mskd.flux.screen.artwork.ArtworkEvent
import com.mskd.flux.screen.artwork.ArtworkIntent
import com.mskd.flux.screen.artwork.ArtworkViewModel
import com.mskd.flux.screens.artwork.composables.ArtworkContentLarge
import com.mskd.flux.screens.artwork.composables.ArtworkContentRegular
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.ErrorScreen
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.ResetProgressDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.utils.ExternalPlayer
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.WebLink
import com.mskd.flux.utils.rememberExternalPlayerLauncher
import com.mskd.flux.utils.rememberScreenDimensions
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_eraser
import flux.shared.generated.resources.mark_previous_episodes_as_watched
import flux.shared.generated.resources.more_info
import flux.shared.generated.resources.oups_an_error_occured
import flux.shared.generated.resources.reset_progress
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ArtworkScreen(
    artworkId: Long,
    season: Int?,
    colorScheme: ColorScheme,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: ArtworkViewModel = koinViewModel(parameters = { parametersOf(artworkId, season) })
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

    AnimatedContent(
        targetState = uiState.state,
        label = "ArtworkScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content_${state.content.fullArtwork.artwork.id}"
            }
        }
    ) { state ->

        when (state) {
            State.Loading -> LoadingScreen()
            is State.Error -> {
                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(ArtworkIntent.OnBackTap) }
                )
            }
            is State.Content -> {
                val content = state.content
                MaterialTheme(colorScheme = colorScheme) {
                    ArtworkScreenContent(
                        fullArtwork = content.fullArtwork,
                        selectedMedia = content.selectedMedia,
                        selectedSeason = content.selectedSeason,
                        expandedEpisodeId = content.expandedEpisodeId,
                        dialog = content.dialog,
                        sendIntent = viewModel::handleIntent
                    )
                }
            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtworkScreenContent(
    fullArtwork: FullArtwork,
    selectedMedia: Media,
    selectedSeason: Int?,
    expandedEpisodeId: Long?,
    dialog: ArtworkDialog?,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val isLargeScreen = rememberScreenDimensions().isLarge

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

    val title = when {
        isLargeScreen -> null
        fullArtwork is FullArtwork.FullShow -> (fullArtwork.seasons.find { it.season == selectedSeason }?.title ?: "").ifBlank { fullArtwork.artwork.title }
        else -> null
    }

    FluxScaffold(
        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
        title = title,
        topAppBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = if (title?.isNotBlank() == true) MaterialTheme.colorScheme.background else Color.Transparent,
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
                selectedMedia = selectedMedia,
                selectedSeason = selectedSeason,
                expandedEpisodeId = expandedEpisodeId,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        } else {
            ArtworkContentRegular(
                fullArtwork = fullArtwork,
                selectedMedia = selectedMedia,
                selectedSeason = selectedSeason,
                expandedEpisodeId = expandedEpisodeId,
                scaffoldInnerPadding = innerPadding,
                sendIntent = sendIntent,
            )
        }

    }

    if (dialog is ArtworkDialog.EpisodeStatusConfirmation) {
        FluxDialog(
            content = {
                Text.Body.Large(text = stringResource(Res.string.mark_previous_episodes_as_watched))
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
                text = stringResource(Res.string.more_info),
                onClick = {
                    sendIntent(ArtworkIntent.OpenArtworkInfo)
                    onDismissRequest()
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = stringResource(Res.string.more_info)) },
            ),
            FluxDropDownMenuItem(
                text = stringResource(Res.string.reset_progress),
                onClick = {
                    sendIntent(ArtworkIntent.ShowResetProgressDialog)
                    onDismissRequest()
                },
                leadingIcon = { Icon(painter = painterResource(Res.drawable.ic_eraser), contentDescription = stringResource(Res.string.reset_progress)) },
            )
        )
    )

}

@FluxPreview
@Composable
fun ArtworkScreenContent_Preview() {
    FluxTheme {
        ArtworkScreenContent(
            fullArtwork = MediaMockups.fullShow,
            selectedMedia = MediaMockups.episode1,
            selectedSeason = MediaMockups.episode1.season,
            expandedEpisodeId = null,
            dialog = null,
            sendIntent = {}
        )
    }
}