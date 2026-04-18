package com.mskd.flux.screens.player

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.util.Log
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
import com.mskd.flux.screens.player.controllers.rememberPlayerScaleEffects
import com.mskd.flux.screens.player.controllers.rememberPlayerStateHolder
import com.mskd.flux.screens.player.controllers.rememberWindowStateHolder
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.LandscapePreview
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
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var currentVideoSize by remember { mutableStateOf(player.videoSize) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                currentVideoSize = videoSize
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
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
            player = player
        )

        PlayerSubtitles(
            modifier = Modifier
                .layoutId("subtitles")
                .padding(bottom = Ui.Space.LARGE),
            subtitles = subtitles,
            smallText = isPortrait
        )

        PlayerInterface(
            modifier = Modifier.layoutId("playerInterface"),
            media = media,
            player = player,
            controlsState = controlsState,
            rewindAndForward = rewindAndForward,
            sendIntent = sendIntent,
        )

        PlayerSeekOverlay(
            layoutIdLeft = "leftSeekOverlay",
            layoutIdRight = "rightSeekOverlay",
            seekOverlay = seekOverlay
        )

        PlayerAmbientOverlay(
            modifier = Modifier.layoutId("ambientOverlay"),
            ambientOverlay = ambientOverlay
        )

    }

    PlayerSettings(
        controlsState = controlsState,
        tracksState = tracksState,
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

        Log.d("TEST", "videoSize.width ${videoSize.width}")
        Log.d("TEST", "videoSize.height ${videoSize.height}")
        Log.d("TEST", "ratio $playerRatio")

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
    AppTheme {
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