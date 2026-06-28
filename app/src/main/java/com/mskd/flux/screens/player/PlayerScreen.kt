package com.mskd.flux.screens.player

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.mskd.flux.model.State
import com.mskd.flux.model.enums.Side
import com.mskd.flux.screen.player.PlayerIntent
import com.mskd.flux.screen.player.PlayerUiContent
import com.mskd.flux.screen.player.PlayerViewModel
import com.mskd.flux.screen.player.rememberPlayerScaleEffects
import com.mskd.flux.screen.player.rememberWindowStateHolder
import com.mskd.flux.screens.player.composables.PlayerSideEffects
import com.mskd.flux.screens.player.composables.playerInterface.PlayerAmbientOverlay
import com.mskd.flux.screens.player.composables.playerInterface.PlayerInterface
import com.mskd.flux.screens.player.composables.playerInterface.PlayerSeekOverlay
import com.mskd.flux.screens.player.composables.playerInterface.PlayerSubtitles
import com.mskd.flux.screens.player.composables.settings.PlayerSettings
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.ErrorScreen
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.LandscapePreview
import com.mskd.flux.utils.extensions.description
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.oups_an_error_occured
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.seconds

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    mediaId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel<Player> = koinViewModel(parameters = { parametersOf(mediaId) })
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subtitles by viewModel.subtitles.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val content = (uiState.state as? State.Content)?.content

    val windowStateHolder = rememberWindowStateHolder()
    var interfaceVisibilityCountdown by remember { mutableIntStateOf(3) }

    PlayerSideEffects(
        viewModel = viewModel,
        windowStateHolder = windowStateHolder,
        showInterface = content?.showInterface ?: false,
        onBack = onBack,
        isPlayingContent = { content?.isPlaying == true },
    )

    // Automatically hide interface after 5 seconds
    LaunchedEffect(content?.showInterface, content?.settingsSheet) {
        if (content?.showInterface == true && content.settingsSheet == null) {
            while (interfaceVisibilityCountdown > 0) {
                delay(1.seconds)
                interfaceVisibilityCountdown--
            }
            viewModel.handleIntent(PlayerIntent.ChangeInterfaceVisibility)
        }
    }

    BackHandler(enabled = true) {
        viewModel.handleIntent(PlayerIntent.OnBackTap)
    }

    AnimatedContent(
        targetState = uiState.state,
        label = "PlayerScreenState",
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
                    onBackButtonTap = { viewModel.handleIntent(PlayerIntent.OnBackTap) }
                )

            }
            is State.Content -> {

                val focusRequester = remember { FocusRequester() }

                PlayerContent(
                    content = state.content,
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
    content: PlayerUiContent<Player>,
    subtitles: () -> List<String?>,
    progress: () -> Long,
    focusRequester: FocusRequester,
    sendIntent: (PlayerIntent) -> Unit
) {

    val infoWindow = LocalWindowInfo.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var currentVideoSize by remember { mutableStateOf(content.player.videoSize) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    DisposableEffect(content.player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                currentVideoSize = videoSize
            }
        }
        content.player.addListener(listener)
        onDispose { content.player.removeListener(listener) }
    }

    val scaleState = rememberPlayerScaleEffects(
        videoSize = currentVideoSize,
        containerSize = infoWindow.containerSize,
        isPortrait = isPortrait
    )

    val animatedScale by animateFloatAsState(
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

                    else -> false
                }

            }
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
            },
        constraintSet = playerConstraintSet(videoSize = currentVideoSize)
    ) {

        ContentFrame(
            modifier = Modifier
                .layoutId("player")
                .graphicsLayer(
                    scaleX = animatedScale,
                    scaleY = animatedScale
                )
            ,
            player = content.player
        )

        if (!content.isInPip) {

            PlayerSubtitles(
                modifier = Modifier
                    .layoutId("subtitles")
                    .padding(bottom = FluxUI.Space.large),
                subtitles = subtitles,
                smallText = isPortrait
            )

            PlayerInterface(
                modifier = Modifier.layoutId("playerInterface"),
                media = content.media,
                content = content,
                progress = progress,
                sendIntent = sendIntent,
            )

            PlayerSeekOverlay(
                layoutIdLeft = "leftSeekOverlay",
                layoutIdRight = "rightSeekOverlay",
                seekOverlay = { content.seekOverlay }
            )

            PlayerAmbientOverlay(
                modifier = Modifier.layoutId("ambientOverlay"),
                ambientOverlay = { content.ambientOverlay }
            )

        }

    }

    PlayerSettings(
        content = content,
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

@OptIn(UnstableApi::class)
@Composable
//@FluxPreview
@LandscapePreview
fun PlayerContent_Preview() {
    FluxTheme {
        ConstraintLayout(
            modifier = Modifier.fillMaxSize(),
            constraintSet = playerConstraintSet(videoSize = VideoSize(16, 9))
        ) {

            Box(
                modifier = Modifier
                    .layoutId("player")
                    .border(width = .5.dp, color = MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text.Label.Medium(text = "player")
            }

            Box(
                modifier = Modifier
                    .layoutId("subtitles")
                    .border(width = .5.dp, color = MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text.Label.Medium(text = "subtitles")
            }

            Box(
                modifier = Modifier
                    .layoutId("playerInterface")
                    .border(width = .5.dp, color = MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text.Label.Medium(text = "playerInterface")
            }

            Box(
                modifier = Modifier
                    .layoutId("leftSeekOverlay")
                    .border(width = .5.dp, color = MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text.Label.Medium(text = "leftSeekOverlay")
            }

            Box(
                modifier = Modifier
                    .layoutId("rightSeekOverlay")
                    .border(width = .5.dp, color = MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text.Label.Medium(text = "rightSeekOverlay")
            }

            Box(
                modifier = Modifier
                    .layoutId("ambientOverlay")
                    .border(width = .5.dp, color = MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text.Label.Medium(text = "ambientOverlay")
            }

        }
    }
}