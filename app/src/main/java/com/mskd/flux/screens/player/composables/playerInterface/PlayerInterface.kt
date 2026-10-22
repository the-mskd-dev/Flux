package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerOverlays
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.screens.player.composables.collectSelectedAsState
import kotlinx.coroutines.flow.StateFlow
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun PlayerInterface(
    modifier: Modifier = Modifier,
    session: PlayerSession<Any>,
    interfaceState: StateFlow<PlayerInterfaceState>,
    overlays: StateFlow<PlayerOverlays>,
    isPlaying: StateFlow<Boolean>,
    progress: () -> Long,
    sendIntent: (PlayerIntent) -> Unit
) {

    val density = LocalDensity.current
    val showInterface by interfaceState.collectSelectedAsState { it.showInterface }
    val isPlayingState = isPlaying.collectAsStateWithLifecycle()
    val chapterButton = overlays.collectSelectedAsState { it.chapterButton }
    var seekBarHeight by remember { mutableStateOf(0.dp) }

    Box(modifier = modifier) {

        AnimatedVisibility(
            visible = showInterface,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        ) {

            ConstraintLayout(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .5f))
                    .fillMaxSize(),
                constraintSet = PlayerInterfaceConstraintSet
            ) {

                PlayerTopBar(
                    modifier = Modifier.layoutId("topBar"),
                    media = session.media,
                    onBackTap = { sendIntent(PlayerIntent.OnBackTap) }
                )

                PlayerSettingsButton(
                    modifier = Modifier.layoutId("settings"),
                    sendIntent = sendIntent
                )

                PlayerControlButtons(
                    modifier = Modifier
                        .layoutId("controlButtons")
                        .animateEnterExit(
                            enter = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ),
                            exit = scaleOut()
                        ),
                    isPlaying = isPlayingState.value,
                    rewindAndForward = session.playerRewind to session.playerForward,
                    sendIntent = sendIntent
                )

                PlayerSeekBar(
                    modifier = Modifier
                        .layoutId("seekBar")
                        .onGloballyPositioned { coordinates ->
                            val height = with(density) { coordinates.size.height.toDp() }
                            if (seekBarHeight != height) seekBarHeight = height
                        },
                    isPlaying = isPlayingState.value,
                    progress = progress,
                    duration = session.duration,
                    sendIntent = sendIntent,
                )

            }

        }

        PlayerChapterButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = FluxUI.Space.large),
            chapterButton = chapterButton.value,
            bottomMargin = {
                if (showInterface) {
                    seekBarHeight + FluxUI.Space.medium
                } else {
                    FluxUI.Space.large
                }
            },
            sendIntent = sendIntent
        )

    }

}

val PlayerInterfaceConstraintSet = ConstraintSet {

    val (topBar, controlButtons, seekBar, settings, nextEpisode) = createRefsFor(
        "topBar",
        "controlButtons",
        "seekBar",
        "settings",
        "nextEpisode"
    )

    constrain(topBar) {
        top.linkTo(parent.top, FluxUI.Space.medium)
        start.linkTo(parent.start)
        end.linkTo(settings.start, FluxUI.Space.medium)
        width = Dimension.fillToConstraints
    }

    constrain(controlButtons) {
        top.linkTo(parent.top)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom)
    }

    constrain(settings) {
        top.linkTo(topBar.top)
        bottom.linkTo(topBar.bottom)
        end.linkTo(parent.end)
    }

    constrain(seekBar) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        bottom.linkTo(parent.bottom, FluxUI.Space.medium)
    }

    constrain(nextEpisode) {
        end.linkTo(parent.end, FluxUI.Space.medium)
        bottom.linkTo(seekBar.top)
    }

}