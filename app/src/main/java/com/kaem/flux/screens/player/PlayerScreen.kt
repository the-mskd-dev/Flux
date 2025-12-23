package com.kaem.flux.screens.player

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.composables.PlayerInterface
import com.kaem.flux.screens.player.composables.PlayerSubtitles
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.component.LoadingScreen
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.findActivity

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

    val isPlaying by stateHolder.isPlaying.collectAsStateWithLifecycle()
    val subtitles by stateHolder.subtitlesState.collectAsStateWithLifecycle()

    val activity = LocalContext.current.findActivity()
    val originalOrientation = remember { activity?.requestedOrientation }

    PlayerWindowController(
        stateHolder = stateHolder,
        showInterface = state.controls.showInterface,
        originalOrientation = originalOrientation
    )

    LaunchedEffect(state.screen) {
        (state.screen as? PlayerScreen.Content)?.let {
            stateHolder.playMedia(it.media)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    PlayerEvent.BackToPreviousScreen -> onBack()
                    is PlayerEvent.SeekRewind -> stateHolder.onFastRewind(event.time)
                    is PlayerEvent.SeekForward -> stateHolder.onFastForward(event.time)
                    is PlayerEvent.UpdateProgress -> stateHolder.updateProgress(event.progress)
                    PlayerEvent.TogglePlayButton -> stateHolder.togglePlayButton()
                }
            }
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
                    showInterface = state.controls.showInterface,
                    isPlaying = isPlaying,
                    subtitles =  { subtitles },
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
    showInterface: Boolean,
    isPlaying: Boolean,
    subtitles: () -> List<Cue>,
    sendIntent: (PlayerIntent) -> Unit
) {

    LifecycleComponent(
        onBackground = {
            if (player.isPlaying) sendIntent(PlayerIntent.TogglePlayButton)
            sendIntent(PlayerIntent.SaveTime(time = player.currentPosition))
        },
        onForeground = {
            if (!player.isPlaying) player.play()
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
            showInterface = showInterface,
            isPlaying = isPlaying,
            player = player,
            sendIntent = sendIntent,
        )

    }

}

@Composable
private fun PlayerWindowController(
    stateHolder: PlayerStateHolder,
    showInterface: Boolean,
    originalOrientation: Int?
) {

    DisposableEffect(Unit) {
        stateHolder.setLandscape()
        onDispose {
            stateHolder.updateSystemBars(true)
            originalOrientation?.let { stateHolder.resetOrientation(originalOrientation) }
        }
    }

    LaunchedEffect(showInterface) {
        stateHolder.updateSystemBars(showInterface)
    }

}