package com.kaem.flux.screens.player

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.SubtitleView
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import com.kaem.flux.R
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.composables.PlayerInterface
import com.kaem.flux.screens.player.composables.PlayerSubtitles
import com.kaem.flux.ui.component.ErrorScreen
import com.kaem.flux.ui.component.LifecycleComponent
import com.kaem.flux.ui.component.LoadingScreen
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.forceScreenOn
import com.kaem.flux.utils.extensions.hideSystemBars
import com.kaem.flux.utils.extensions.setAppInLandscape
import com.kaem.flux.utils.extensions.setAppOrientation
import com.kaem.flux.utils.extensions.showSystemBars
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Immutable
data class InterfaceState(
    val showInterface: Boolean,
    val isPlaying: Boolean
)

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel<PlayerViewModel, PlayerViewModel.Factory>(
        creationCallback = { factory -> factory.create(mediaId = mediaId) }
    )
) {

    val activity = LocalActivity.current as ComponentActivity
    val orientation = remember { activity.requestedOrientation }

    val screenState by remember(viewModel) {
        viewModel.uiState
            .map { it.screen }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = PlayerScreen.Loading)

    val subtitlesState = remember(viewModel) {
        viewModel.uiState
            .map { it.subtitles }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val controlsState by remember(viewModel) {
        viewModel.uiState
            .map { InterfaceState(it.showInterface, it.isPlaying) } // Voir la data class plus bas
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = InterfaceState(showInterface = false, isPlaying = false))

    DisposableEffect(Unit) {
        activity.setAppInLandscape()
        activity.forceScreenOn(true)
        onDispose {
            activity.forceScreenOn(false)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                PlayerEvent.BackToPreviousScreen -> {
                    activity.setAppOrientation(orientation)
                    onBack()
                }
            }
        }
    }

    LaunchedEffect(controlsState.showInterface) {
        if (controlsState.showInterface) activity.showSystemBars() else activity.hideSystemBars()
    }

    Crossfade(screenState) { screen ->
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
                    player = viewModel.player,
                    showInterface = controlsState.showInterface,
                    isPlaying = controlsState.isPlaying,
                    subtitlesState = subtitlesState,
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
    subtitlesState: State<List<Cue>>,
    sendIntent: (PlayerIntent) -> Unit
) {

    val activity = LocalActivity.current as ComponentActivity

    LaunchedEffect(media) {
        player.setMediaItem(MediaItem.fromUri(media.file.path.toUri()))
        player.seekTo(media.currentTime)
        player.prepare()
    }

    LifecycleComponent(
        onDispose = {
            activity.showSystemBars()
            player.release()
        },
        onBackground = {
            player.pause()
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

        /*AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )*/

        ContentFrame(
            modifier = Modifier.fillMaxSize(),
            player = player
        )

        PlayerSubtitles(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = Ui.Space.LARGE),
            subtitlesState = subtitlesState
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