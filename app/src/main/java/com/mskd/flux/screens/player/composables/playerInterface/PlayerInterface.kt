package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.presentation.PlayerInterfaceState
import com.mskd.flux.features.player.presentation.PlayerOverlays
import com.mskd.flux.features.player.presentation.PlayerSession
import com.mskd.flux.screens.player.composables.collectSelectedAsState
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.coroutines.flow.StateFlow

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

    val showInterface by interfaceState.collectSelectedAsState { it.showInterface }
    val isPlayingState = isPlaying.collectAsStateWithLifecycle()
    val chapterButton = overlays.collectSelectedAsState { it.chapterButton }
    var seekBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // The Interface stays composed: its appearance is replayed by this transition, read in the draw phase only
    val transition = updateTransition(targetState = showInterface, label = "PlayerInterfaceVisibility")
    val alpha = transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) },
        label = "PlayerInterfaceAlpha"
    ) { if (it) 1f else 0f }
    val buttonsScale = transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            } else {
                spring(stiffness = Spring.StiffnessMediumLow)
            }
        },
        label = "PlayerControlButtonsScale"
    ) { if (it) 1f else 0f }
    val buttonsSlide = transition.animateFloat(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
        label = "PlayerControlButtonsSlide"
    ) { if (it) 1f else 0f }

    // Visible, appearing or fading out (as with AnimatedVisibility): once fully hidden, the wavy track is
    // no longer composed and no element receives click or focus. Disabled styling never shows during the fade.
    val isActive = transition.currentState || transition.targetState

    Box(modifier = modifier) {

        ConstraintLayout(
            modifier = Modifier
                .graphicsLayer { this.alpha = alpha.value }
                .focusProperties { canFocus = isActive }
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = .5f))
                .fillMaxSize(),
            constraintSet = PlayerInterfaceConstraintSet
        ) {

            PlayerTopBar(
                modifier = Modifier.layoutId("topBar"),
                media = session.media,
                enabled = isActive,
                onBackTap = { sendIntent(PlayerIntent.OnBackTap) }
            )

            PlayerSettingsButton(
                modifier = Modifier.layoutId("settings"),
                enabled = isActive,
                sendIntent = sendIntent
            )

            // A fully scaled-down Row cannot be hit, so hidden buttons receive no click
            PlayerControlButtons(
                modifier = Modifier
                    .layoutId("controlButtons")
                    .graphicsLayer {
                        val scale = buttonsScale.value
                        scaleX = scale
                        scaleY = scale
                    },
                isPlaying = isPlayingState.value,
                rewindAndForward = session.playerRewind to session.playerForward,
                slide = { buttonsSlide.value },
                sendIntent = sendIntent
            )

            PlayerSeekBar(
                modifier = Modifier
                    .layoutId("seekBar")
                    .onSizeChanged { size ->
                        val height = with(density) { size.height.toDp() }
                        if (seekBarHeight != height) seekBarHeight = height
                    },
                isPlaying = isPlayingState.value,
                progress = progress,
                duration = session.duration,
                enabled = isActive,
                showWave = isActive,
                sendIntent = sendIntent,
            )

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