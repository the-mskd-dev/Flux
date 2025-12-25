package com.kaem.flux.screens.player

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.composables.playerInterface.PlayerInterface
import com.kaem.flux.screens.player.composables.playerInterface.PlayerSubtitles
import com.kaem.flux.screens.player.composables.settings.PlayerSettings
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.component.LoadingScreen
import com.kaem.flux.ui.theme.Ui

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel<PlayerViewModel, PlayerViewModel.Factory>(
        creationCallback = { factory -> factory.create(mediaId = mediaId) }
    )
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stateHolder = rememberPlayerStateHolder()
    val subtitles by stateHolder.subtitles.collectAsStateWithLifecycle()

    PlayerSideEffects(
        viewModel = viewModel,
        stateHolder = stateHolder,
        showInterface = state.controls.showInterface,
        onBack = onBack
    )

    LaunchedEffect(state.screen) {
        (state.screen as? PlayerScreen.Content)?.let {
            stateHolder.playMedia(it.media)
        }
    }

    Crossfade(targetState = state.screen, label = "PlayerScreenState") { screen ->
        when (screen) {
            PlayerScreen.Loading -> LoadingScreen()
            PlayerScreen.Error -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(PlayerIntent.OnBackTap()) }
                )
            }
            is PlayerScreen.Content -> {
                PlayerContent(
                    media = screen.media,
                    player = stateHolder.player,
                    subtitles =  { subtitles },
                    controlsState = { state.controls },
                    tracksState = { state.tracks },
                    sendIntent = viewModel::handleIntent
                )
            }
        }

    }

}

@OptIn(UnstableApi::class)
@Composable
fun PlayerContent(
    media: Media,
    player: Player,
    subtitles: () -> List<Cue>,
    controlsState: () -> PlayerUiState.Controls,
    tracksState: () -> PlayerUiState.Tracks,
    sendIntent: (PlayerIntent) -> Unit
) {

    var wasPlayingBeforeBackground by remember { mutableStateOf(false) }

    LifecycleComponent(
        onBackground = {
            wasPlayingBeforeBackground = player.isPlaying
            if (player.isPlaying) sendIntent(PlayerIntent.TogglePlayButton)
            sendIntent(PlayerIntent.SaveTime(time = player.currentPosition))
        },
        onForeground = {
            if (wasPlayingBeforeBackground) player.play()
        }
    )

    BackHandler(enabled = true) {
        sendIntent(PlayerIntent.OnBackTap(time = player.currentPosition))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                sendIntent(PlayerIntent.ShowInterface)
            }
    ) {

        ContentFrame(
            modifier = Modifier.fillMaxSize(),
            player = player
        )

        PlayerSubtitles(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Ui.Space.LARGE),
            subtitles = subtitles
        )

        PlayerInterface(
            media = media,
            player = player,
            controlsState = controlsState,
            sendIntent = sendIntent,
        )

    }

    PlayerSettings(
        controlsState = controlsState,
        tracksState = tracksState,
        sendIntent = sendIntent
    )

}