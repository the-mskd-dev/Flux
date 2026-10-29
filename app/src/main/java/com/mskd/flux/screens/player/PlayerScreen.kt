package com.mskd.flux.screens.player

import android.content.res.Configuration
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.ContentFrame
import com.mskd.flux.core.model.core.State
import com.mskd.flux.core.model.enums.Side
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.features.player.presentation.AmbientOverlay
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerOverlays
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.features.player.presentation.PlayerViewModel
import com.mskd.flux.features.player.presentation.WindowStateHolder
import com.mskd.flux.features.player.presentation.rememberPlayerScaleEffects
import com.mskd.flux.features.player.presentation.rememberWindowStateHolder
import com.mskd.flux.screens.player.composables.PlayerSideEffects
import com.mskd.flux.screens.player.composables.collectSelectedAsState
import com.mskd.flux.screens.player.composables.playerInterface.PlayerAmbientOverlay
import com.mskd.flux.screens.player.composables.playerInterface.PlayerInterface
import com.mskd.flux.screens.player.composables.playerInterface.PlayerSeekOverlay
import com.mskd.flux.screens.player.composables.playerInterface.PlayerSubtitles
import com.mskd.flux.screens.player.composables.settings.PlayerSettings
import com.mskd.flux.ui.components.ErrorScreen
import com.mskd.flux.ui.components.LoadingScreen
import com.mskd.flux.ui.global.BackGesture
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.description
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.oups_an_error_occured
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    params: PlayerParams,
    onBack: () -> Unit,
    viewModel: PlayerViewModel<Player> = koinViewModel(parameters = { parametersOf(params) })
) {

    val session by viewModel.session.collectAsStateWithLifecycle()
    val subtitles by viewModel.subtitles.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()

    val windowStateHolder = rememberWindowStateHolder()
    var interfaceVisibilityCountdown by remember { mutableIntStateOf(3) }

    PlayerSideEffects(
        viewModel = viewModel,
        windowStateHolder = windowStateHolder,
        onBack = onBack,
    )

    // Automatically hide interface after 5 seconds
    val showInterface by viewModel.interfaceState.collectSelectedAsState { it.showInterface }
    val hasSettingsSheet by viewModel.interfaceState.collectSelectedAsState { it.settingsSheet != null }
    val hasContent = session is State.Content
    LaunchedEffect(hasContent, showInterface, hasSettingsSheet) {
        if (hasContent && showInterface && !hasSettingsSheet) {
            while (interfaceVisibilityCountdown > 0) {
                delay(1.seconds)
                interfaceVisibilityCountdown--
            }
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
        }
    }

    BackGesture {
        interfaceVisibilityCountdown = 3
        viewModel.handleIntent(PlayerIntent.OnBackTap)
    }

    AnimatedContent(
        targetState = session,
        label = "CatalogScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                is State.Loading -> "loading"
                is State.Error -> "error"
                is State.Content -> "content_${state.content.media.mediaId}"
            }
        }
    ) { state ->
        when (state) {
            is State.Loading -> LoadingScreen()
            is State.Error -> {

                ErrorScreen(
                    message = stringResource(Res.string.oups_an_error_occured),
                    description = state.description(),
                    onBackButtonClick = { viewModel.handleIntent(PlayerIntent.OnBackTap) }
                )

            }
            is State.Content -> {

                val focusRequester = remember { FocusRequester() }

                PlayerContent(
                    session = state.content,
                    interfaceState = viewModel.interfaceState,
                    overlays = viewModel.overlays,
                    isPlaying = viewModel.isPlaying,
                    volume = viewModel.volume,
                    windowStateHolder = windowStateHolder,
                    subtitles = { subtitles },
                    progress = { progress },
                    focusRequester = focusRequester,
                    sendIntent = {
                        interfaceVisibilityCountdown = 3
                        viewModel.handleIntent(it)
                        focusRequester.requestFocus()
                    }
                )
            }
        }
    }

}

@OptIn(UnstableApi::class)
@Composable
fun PlayerContent(
    session: PlayerSession<Player>,
    interfaceState: StateFlow<PlayerInterfaceState>,
    overlays: StateFlow<PlayerOverlays>,
    isPlaying: StateFlow<Boolean>,
    volume: Flow<Int>,
    windowStateHolder: WindowStateHolder,
    subtitles: () -> List<String?>,
    progress: () -> Long,
    focusRequester: FocusRequester,
    sendIntent: (PlayerIntent) -> Unit
) {

    val infoWindow = LocalWindowInfo.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var currentVideoSize by remember { mutableStateOf(session.player.videoSize) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    DisposableEffect(session.player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                currentVideoSize = videoSize
            }
        }
        session.player.addListener(listener)
        onDispose { session.player.removeListener(listener) }
    }

    val scaleState = rememberPlayerScaleEffects(
        videoSize = currentVideoSize,
        containerSize = infoWindow.containerSize,
        isPortrait = isPortrait
    )

    val animatedScale = animateFloatAsState(
        targetValue = scaleState.targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Player full screen animation"
    )

    val stateTransform = rememberTransformableState { _, zoomChange, _, _ ->
        when {
            zoomChange < 1f -> scaleState.toggleFill(false)
            zoomChange > 1f -> scaleState.toggleFill(true)
        }
    }

    val currentSendIntent by rememberUpdatedState(sendIntent)
    val currentWindowStateHolder by rememberUpdatedState(windowStateHolder)
    val isInPip by interfaceState.collectSelectedAsState { it.isInPip }
    val seekOverlay by overlays.collectSelectedAsState { it.seekOverlay }

    // The Ambient overlay is local to the screen: brightness is applied here, volume comes back from the VM
    var ambientOverlay by remember { mutableStateOf<AmbientOverlay?>(null) }
    var ambientTick by remember { mutableIntStateOf(0) }
    val showAmbient = remember {
        { type: AmbientOverlay.Type, value: Int ->
            ambientOverlay = AmbientOverlay(type = type, value = value)
            ambientTick++
        }
    }

    // Read through snapshotFlow so that ticks do not recompose the screen
    LaunchedEffect(Unit) {
        snapshotFlow { ambientTick }.collectLatest {
            if (it == 0) return@collectLatest
            delay(1.seconds)
            ambientOverlay = null
        }
    }

    val currentShowAmbient by rememberUpdatedState(showAmbient)

    LaunchedEffect(volume) {
        volume.collect { showAmbient(AmbientOverlay.Type.VOLUME, it) }
    }

    ConstraintLayout(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .fillMaxSize()
            .background(Color.Black)
            .transformable(state = stateTransform)
            .onKeyEvent { keyEvent ->

                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                when (keyEvent.key) {
                    Key.Spacebar -> {
                        sendIntent(PlayerIntent.TogglePlayButton); true
                    }

                    Key.DirectionLeft, Key.MediaRewind -> {
                        sendIntent(PlayerIntent.OnFastRewind); true
                    }

                    Key.DirectionRight, Key.MediaFastForward -> {
                        sendIntent(PlayerIntent.OnFastForward); true
                    }

                    Key.MediaPrevious -> {
                        sendIntent(PlayerIntent.OnMediaPrevious); true
                    }

                    Key.MediaNext -> {
                        // A held key must not skip several Episodes
                        if (keyEvent.nativeKeyEvent.repeatCount == 0) sendIntent(PlayerIntent.OnMediaNext)
                        true
                    }

                    else -> false
                }

            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val edgeMargin = 32.dp.toPx()
                        if (offset.x > edgeMargin && offset.x < (size.width - edgeMargin)) {
                            currentSendIntent(PlayerIntent.ChangeInterfaceVisibility)
                        }
                    },
                    onDoubleTap = { offset ->
                        val width = size.width
                        if (offset.x < (width * .4f)) {
                            currentSendIntent(PlayerIntent.OnFastRewind)
                        } else if (offset.x > (width * .6f)) {
                            currentSendIntent(PlayerIntent.OnFastForward)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var side = Side.LEFT
                var pendingDelta = 0f

                fun flush() {
                    val delta = pendingDelta
                    if (delta == 0f) return
                    pendingDelta = 0f
                    when (side) {
                        Side.LEFT -> currentWindowStateHolder.changeBrightness(delta)
                            ?.let { currentShowAmbient(AmbientOverlay.Type.BRIGHTNESS, it) }
                        Side.RIGHT -> currentSendIntent(PlayerIntent.OnVolumeChange(delta))
                    }
                }

                coroutineScope {
                    // Finger moves are accumulated and applied at most once per frame
                    val frameRequests = Channel<Unit>(Channel.CONFLATED)
                    launch {
                        for (request in frameRequests) {
                            withFrameNanos { }
                            flush()
                        }
                    }
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            side = if (offset.x > size.width / 2) Side.RIGHT else Side.LEFT
                            pendingDelta = 0f
                        },
                        onDragEnd = { flush() },
                        onDragCancel = { flush() },
                        onVerticalDrag = { change, dragAmount ->
                            pendingDelta += (-dragAmount / size.height) * 2
                            frameRequests.trySend(Unit)
                            change.consume()
                        },
                    )
                }
            },
        constraintSet = playerConstraintSet(videoSize = currentVideoSize)
    ) {

        ContentFrame(
            modifier = Modifier
                .layoutId("player")
                .graphicsLayer {
                    val scale = animatedScale.value
                    scaleX = scale
                    scaleY = scale
                },
            player = session.player
        )

        if (!isInPip) {

            PlayerSubtitles(
                modifier = Modifier
                    .layoutId("subtitles")
                    .padding(bottom = FluxUI.Space.large),
                subtitles = subtitles,
                smallText = isPortrait
            )

            PlayerInterface(
                modifier = Modifier.layoutId("playerInterface"),
                session = session,
                interfaceState = interfaceState,
                overlays = overlays,
                isPlaying = isPlaying,
                progress = progress,
                sendIntent = sendIntent,
            )

            PlayerSeekOverlay(
                layoutIdLeft = "leftSeekOverlay",
                layoutIdRight = "rightSeekOverlay",
                seekOverlay = { seekOverlay }
            )

            PlayerAmbientOverlay(
                modifier = Modifier.layoutId("ambientOverlay"),
                ambientOverlay = { ambientOverlay }
            )

        }

    }

    PlayerSettings(
        session = session,
        interfaceState = interfaceState,
        overlays = overlays,
        sendIntent = sendIntent
    )

}

@OptIn(UnstableApi::class)
@Composable
fun playerConstraintSet(videoSize: VideoSize) = remember( videoSize) {
    ConstraintSet {

        val playerRatio = if (videoSize.width > 0 && videoSize.height > 0) {
            "${videoSize.width}:${videoSize.height}"
        } else {
            "16:9"
        }

        val (player, subtitles, playerInterface, leftSeekOverlay, rightSeekOverlay, ambientOverlay) = createRefsFor(
            "player",
            "subtitles",
            "playerInterface",
            "leftSeekOverlay",
            "rightSeekOverlay",
            "ambientOverlay"
        )

        val leftGuideline = createGuidelineFromStart(.33f)
        val rightGuideline = createGuidelineFromEnd(.33f)

        constrain(player) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
            height = Dimension.ratio(playerRatio)
        }

        constrain(subtitles) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(player.bottom)
        }

        constrain(playerInterface) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        }

        constrain(leftSeekOverlay) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(leftGuideline)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        }

        constrain(rightSeekOverlay) {
            top.linkTo(parent.top)
            start.linkTo(rightGuideline)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            width = Dimension.fillToConstraints
            height = Dimension.fillToConstraints
        }

        constrain(ambientOverlay) {
            centerTo(parent)
        }

    }
}