package com.mskd.flux.screens.artwork

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.core.State
import com.mskd.flux.features.artwork.presentation.ArtworkDialog
import com.mskd.flux.features.artwork.presentation.ArtworkEvent
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.features.artwork.presentation.ArtworkViewModel
import com.mskd.flux.features.artwork.ui.ArtworkContentLarge
import com.mskd.flux.features.artwork.ui.ArtworkContentRegular
import com.mskd.flux.features.artwork.ui.composables.ArtworkDropDownMenu
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.Route.Player
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.components.LoadingScreen
import com.mskd.flux.ui.components.ResetProgressDialog
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.modal.dialog.FluxDialog
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.utils.rememberExternalPlayerAction
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.mark_previous_episodes_as_watched
import flux.shared.generated.resources.oups_an_error_occured
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

    val launchExternalPlayer = rememberExternalPlayerAction(
        onProgressResult = { progress -> viewModel.handleIntent(ArtworkIntent.OnExternalPlayerResult(progress = progress)) },
        onFallbackToInternal = { media -> viewModel.handleIntent(ArtworkIntent.PlayMedia(media = media, forceInternal = true)) }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                ArtworkEvent.BackToPreviousScreen -> onBack()
                is ArtworkEvent.PlayMedia -> {
                    if (event.externalPlayer)
                        launchExternalPlayer(event.media)
                    else
                        navigate(Player(params = PlayerParams.fromMedia(event.media)))
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
                    onBackButtonClick = { viewModel.handleIntent(ArtworkIntent.OnBackTap) }
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

    val title = when {
        isLargeScreen -> null
        fullArtwork is FullArtwork.FullShow -> (fullArtwork.seasons.find { it.season == selectedSeason }?.title ?: "").ifBlank { fullArtwork.artwork.title }
        else -> null
    }

    FluxScaffold(
        title = title,
        animatedTitle = true,
        actions = {
            IconButton(
                onClick = { showMenu = true },
                content = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "menu button"
                    )
                }
            )

            ArtworkDropDownMenu(
                isVisible = showMenu,
                fullArtwork = fullArtwork,
                onDismissRequest = { showMenu = false },
                sendIntent = sendIntent
            )

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

    FluxDialog(
        isVisible = dialog is ArtworkDialog.EpisodeStatusConfirmation,
        content = {
            Text.Content.Body(text = stringResource(Res.string.mark_previous_episodes_as_watched))
        },
        onDismiss = { sendIntent(ArtworkIntent.CloseDialog) },
        onValidate = { sendIntent(ArtworkIntent.MarkPreviousEpisodesAsWatched) }
    )

    if (dialog is ArtworkDialog.ResetProgressConfirmation) {
        ResetProgressDialog(
            onValidate = { sendIntent(ArtworkIntent.ResetProgress) },
            onDismiss = { sendIntent(ArtworkIntent.CloseDialog) }
        )
    }

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