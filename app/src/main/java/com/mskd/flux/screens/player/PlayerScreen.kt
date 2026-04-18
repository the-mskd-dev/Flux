package com.mskd.flux.screens.player

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.text.Cue
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import com.mskd.flux.R
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.screens.player.composables.playerInterface.PlayerAmbientOverlay
import com.mskd.flux.screens.player.composables.playerInterface.PlayerInterface
import com.mskd.flux.screens.player.composables.playerInterface.PlayerSeekOverlay
import com.mskd.flux.screens.player.composables.playerInterface.PlayerSubtitles
import com.mskd.flux.screens.player.composables.settings.PlayerSettings
import com.mskd.flux.screens.player.controllers.PlayerSideEffects
import com.mskd.flux.screens.player.controllers.rememberPlayerStateHolder
import com.mskd.flux.screens.player.controllers.rememberWindowStateHolder
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.enums.Side
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
    val playerStateHolder = rememberPlayerStateHolder()
    val windowStateHolder = rememberWindowStateHolder()
    val subtitles by playerStateHolder.subtitles.collectAsStateWithLifecycle()
    var interfaceVisibilityCountdown by remember { mutableIntStateOf(3) }

    PlayerSideEffects(
        viewModel = viewModel,
        stateHolder = playerStateHolder,
        windowStateHolder = windowStateHolder,
        showInterface = state.controls.showInterface,
        onBack = onBack
    )

    LaunchedEffect(state.screen) {
        (state.screen as? PlayerScreen.Content)?.let {
            playerStateHolder.playMedia(it.media)
        }
    }

    // Automatically hide interface after 5 seconds
    LaunchedEffect(state.controls) {
        if (state.controls.showInterface && state.controls.settingsSheet == null) {
            while (interfaceVisibilityCountdown > 0) {
                delay(1.seconds)
                interfaceVisibilityCountdown--
            }
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
        }
    }

    BackHandler(enabled = true) {
        viewModel.handleIntent(PlayerIntent.OnBackTap(time = playerStateHolder.player.currentPosition))
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
                    player = playerStateHolder.player,
                    subtitles =  { subtitles },
                    rewindAndForward = { state.playerRewind to state.playerForward },
                    controlsState = { state.controls },
                    tracksState = { state.tracks },
                    seekOverlay = { state.seekOverlay },
                    ambientOverlay = { state.ambientOverlay },
                    sendIntent = {
                        interfaceVisibilityCountdown = 3
                        viewModel.handleIntent(it)
                    }
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
    rewindAndForward: () -> Pair<Int, Int>,
    controlsState: () -> PlayerUiState.Controls,
    tracksState: () -> PlayerUiState.Tracks,
    seekOverlay: () -> PlayerUiState.SeekOverlay?,
    ambientOverlay: () -> PlayerUiState.AmbientOverlay?,
    sendIntent: (PlayerIntent) -> Unit
) {

    val infoWindow = LocalWindowInfo.current
    val configuration = LocalConfiguration.current

    val fillScale = remember(infoWindow.containerSize, player.videoSize) {
        val videoSize = player.videoSize
        val containerSize = infoWindow.containerSize

        if (videoSize.width <= 0 || videoSize.height <= 0) {
            1f
        } else {
            val screenWidth = containerSize.width.toFloat()
            val screenHeight = containerSize.height.toFloat()

            val videoWidth = videoSize.width.toFloat()
            val videoHeight = videoSize.height.toFloat()

            val widthRatio = screenWidth / videoWidth
            val heightRatio = screenHeight / videoHeight

            maxOf(widthRatio / minOf(widthRatio, heightRatio), heightRatio / minOf(widthRatio, heightRatio))
        }
    }

    var fillPlayer by rememberSaveable { mutableStateOf(false) }
    val scaleTarget = if (fillPlayer) fillScale else 1f

    val animatedScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Player full screen animation"
    )

    val stateTransform = rememberTransformableState { _, zoomChange, _, _ ->
        when {
            configuration.orientation == Configuration.ORIENTATION_PORTRAIT -> return@rememberTransformableState
            zoomChange < 1f -> fillPlayer = false
            zoomChange > 1f -> fillPlayer = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .transformable(state = stateTransform)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val edgeMargin = 32.dp.toPx()
                        if (offset.x > edgeMargin && offset.x < (size.width - edgeMargin)) {
                            sendIntent(PlayerIntent.ChangeInterfaceVisibility)
                        }
                    },
                    onDoubleTap = { offset ->
                        val width = size.width
                        if (offset.x < (width * .4f)) {
                            sendIntent(PlayerIntent.OnFastRewind)
                        } else if (offset.x > (width * .6f)) {
                            sendIntent(PlayerIntent.OnFastForward)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var side = Side.LEFT
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        side = if (offset.x > size.width / 2) Side.RIGHT else Side.LEFT
                    },
                    onVerticalDrag = { change, dragAmount ->
                        val delta = (-dragAmount / size.height) * 2
                        when (side) {
                            Side.LEFT -> sendIntent(PlayerIntent.OnBrightnessChange(delta))
                            Side.RIGHT -> sendIntent(PlayerIntent.OnVolumeChange(delta))
                        }
                        change.consume()
                    },

                )
            }
    ) {

        ContentFrame(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale
                )
            ,
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
            rewindAndForward = rewindAndForward,
            sendIntent = sendIntent,
        )

        PlayerSeekOverlay(seekOverlay = seekOverlay)

        PlayerAmbientOverlay(ambientOverlay = ambientOverlay)

    }

    PlayerSettings(
        controlsState = controlsState,
        tracksState = tracksState,
        sendIntent = sendIntent
    )

}