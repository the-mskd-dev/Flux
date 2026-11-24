package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.buttons.ButtonMotionState
import com.mskd.flux.ui.components.buttons.ControlButton
import com.mskd.flux.ui.components.buttons.ControlButtonsMotion
import com.mskd.flux.ui.components.buttons.PRESS_DAMPING
import com.mskd.flux.ui.components.buttons.PRESS_STIFFNESS
import com.mskd.flux.ui.components.buttons.playerControlMotion
import com.mskd.flux.ui.global.expressivePressScale
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_forward_10
import flux.shared.generated.resources.ic_forward_30
import flux.shared.generated.resources.ic_forward_5
import flux.shared.generated.resources.ic_pause
import flux.shared.generated.resources.ic_play
import flux.shared.generated.resources.ic_rewind_10
import flux.shared.generated.resources.ic_rewind_30
import flux.shared.generated.resources.ic_rewind_5
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerControlButtons(
    modifier: Modifier,
    isPlaying: Boolean,
    rewindAndForward: Pair<Int, Int>,
    slide: () -> Float,
    sendIntent: (PlayerIntent) -> Unit
) {

    val [rewind, forward] = rewindAndForward

    val rewindInteraction = remember { MutableInteractionSource() }
    val playInteraction = remember { MutableInteractionSource() }
    val forwardInteraction = remember { MutableInteractionSource() }

    val motionStates = remember { ControlButton.entries.associateWith { ButtonMotionState() } }

    // A press on one button makes its direct neighbors bounce too.
    LaunchedEffect(motionStates) {
        val motion = ControlButtonsMotion(scope = this, states = motionStates)
        launch { motion.collect(ControlButton.REWIND, rewindInteraction) }
        launch { motion.collect(ControlButton.PLAY, playInteraction) }
        launch { motion.collect(ControlButton.FORWARD, forwardInteraction) }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {

        MediumFloatingActionButton(
            modifier = Modifier
                .playerControlMotion(slideSign = 1f, slide = slide, state = motionStates.getValue(
                    ControlButton.REWIND))
                .expressivePressScale(rewindInteraction, dampingRatio = PRESS_DAMPING, stiffness = PRESS_STIFFNESS),
            interactionSource = rewindInteraction,
            onClick = { sendIntent(PlayerIntent.OnFastRewind) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            val icon = when (rewind) {
                5 -> Res.drawable.ic_rewind_5
                10 -> Res.drawable.ic_rewind_10
                else -> Res.drawable.ic_rewind_30
            }
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                painter = painterResource(icon),
                contentDescription = "rewind button"
            )
        }

        LargeExtendedFloatingActionButton(
            modifier = Modifier
                .playerControlMotion(slideSign = 0f, slide = slide, state = motionStates.getValue(
                    ControlButton.PLAY))
                .expressivePressScale(playInteraction, dampingRatio = PRESS_DAMPING, stiffness = PRESS_STIFFNESS),
            interactionSource = playInteraction,
            onClick = { sendIntent(PlayerIntent.TogglePlayButton) },
            shape = FloatingActionButtonDefaults.largeExtendedFabShape
        ) {
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                painter = painterResource(if (isPlaying) Res.drawable.ic_pause else Res.drawable.ic_play),
                contentDescription = "play button"
            )
        }

        MediumFloatingActionButton(
            modifier = Modifier
                .playerControlMotion(slideSign = -1f, slide = slide, state = motionStates.getValue(
                    ControlButton.FORWARD))
                .expressivePressScale(forwardInteraction, dampingRatio = PRESS_DAMPING, stiffness = PRESS_STIFFNESS),
            interactionSource = forwardInteraction,
            onClick = { sendIntent(PlayerIntent.OnFastForward) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            val icon = when (forward) {
                5 -> Res.drawable.ic_forward_5
                10 -> Res.drawable.ic_forward_10
                else -> Res.drawable.ic_forward_30
            }
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                painter = painterResource(icon),
                contentDescription = "forward button"
            )
        }

    }

}

@FluxPreview
@Composable
fun PlayerControlButtons_Preview() {

    FluxTheme {
        AnimatedContent(targetState = true) {
            PlayerControlButtons(
                modifier = Modifier,
                isPlaying = it,
                rewindAndForward = 5 to 10,
                slide = { 1f },
                sendIntent = {}
            )
        }
    }
}