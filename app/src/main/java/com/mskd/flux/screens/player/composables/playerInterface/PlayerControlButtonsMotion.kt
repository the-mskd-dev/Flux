package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/** Press feedback of the player controls: a little more bounce and a slower return than the default press. */
internal const val PRESS_DAMPING = 0.35f
internal const val PRESS_STIFFNESS = 800f

/** A press must last this long to count as "held": a quick tap never leans the neighbors. */
private const val HOLD_DELAY_MS = 60L

/** The rebound starts this long after the release, when the tapped button accelerates back outward. */
private const val PROPAGATION_DELAY_MS = 40L

private const val HOLD_PULL_DP = 2.5f

/** Outward peak reached by a neighbor when the tapped button rebounds, and its scale dip at that moment. */
private const val REBOUND_OFFSET_PEAK_DP = 8f
private const val REBOUND_SCALE_PEAK = -0.06f

/** Neighbors rebound with a softer spring than the tapped button, so the wave looks delayed and damped. */
private const val NEIGHBOR_DAMPING = 0.38f
private const val NEIGHBOR_STIFFNESS = 450f

/** Compose's default threshold (0.01) would cut the scale rebound (~0.016) short. */
private const val SCALE_VISIBILITY_THRESHOLD = 0.001f

/** The three player controls, in the order they are laid out from left to right. */
internal enum class ControlButton {
    REWIND, PLAY, FORWARD;

    /**
     * Buttons directly next to this one, each with the horizontal direction pointing away from this button
     * (`-1f` = left, `1f` = right).
     */
    fun directNeighbors(): List<Pair<ControlButton, Float>> =
        entries
            .filter { abs(it.ordinal - ordinal) == 1 }
            .map { it to if (it.ordinal > ordinal) 1f else -1f }
}

/**
 * Horizontal offset [button] takes while the [held] buttons are pressed: it leans [pullDp] towards each held direct
 * neighbor. A held button stays under the finger, and pulls from both sides cancel out.
 */
internal fun computeNetHoldPullDp(button: ControlButton, held: Set<ControlButton>, pullDp: Float): Float {
    if (button in held) return 0f
    return held.sumOf { heldButton ->
        val awayFromHeld = heldButton.directNeighbors().firstOrNull { it.first == button }?.second ?: 0f
        (-awayFromHeld * pullDp).toDouble()
    }.toFloat()
}

/**
 * Initial velocity that makes an under-damped spring (mass 1) starting at rest reach [peak] at its first extreme.
 * From x(t) = v0 / wd * e^(-z*w*t) * sin(wd*t), whose first extreme is at tan(wd*t) = wd / (z*w).
 */
internal fun springImpulseVelocity(peak: Float, dampingRatio: Float, stiffness: Float): Float {
    val w = sqrt(stiffness)
    val decay = dampingRatio * w
    val wd = w * sqrt(1f - dampingRatio * dampingRatio)
    val tPeak = atan(wd / decay) / wd
    return peak * wd / (exp(-decay * tPeak) * sin(wd * tPeak))
}

/** Motion a neighbor receives when another button is pressed. Read only during the draw phase. */
internal class ButtonMotionState {
    val offsetDp = Animatable(0f)
    val scaleDelta = Animatable(0f)
}

/**
 * Turns the press interactions of the three buttons into the neighbors' motion: a gentle lean towards a held button,
 * then an elastic outward push with a small scale bounce when it is released.
 */
internal class ControlButtonsMotion(
    private val scope: CoroutineScope,
    private val states: Map<ControlButton, ButtonMotionState>
) {

    private val held = mutableSetOf<ControlButton>()
    private val holdJobs = mutableMapOf<ControlButton, Job>()

    suspend fun collect(button: ControlButton, interactions: InteractionSource) {
        interactions.interactions.collect { interaction: Interaction ->
            when (interaction) {
                is PressInteraction.Press -> onPress(button)
                is PressInteraction.Release, is PressInteraction.Cancel -> onRelease(button)
            }
        }
    }

    private fun onPress(button: ControlButton) {
        holdJobs.remove(button)?.cancel()
        holdJobs[button] = scope.launch {
            delay(HOLD_DELAY_MS)
            held += button
            applyHoldPull()
        }
    }

    private fun onRelease(button: ControlButton) {
        holdJobs.remove(button)?.cancel()
        if (held.remove(button)) applyHoldPull()
        scope.launch {
            delay(PROPAGATION_DELAY_MS)
            button.directNeighbors().forEach { (neighbor, direction) -> rebound(neighbor, direction) }
        }
    }

    private fun applyHoldPull() {
        states.forEach { (button, state) ->
            val target = computeNetHoldPullDp(button, held, HOLD_PULL_DP)
            // Leave untouched buttons alone: restarting their spring would cut a rebound still in flight.
            if (target != state.offsetDp.targetValue) {
                scope.launch { state.offsetDp.animateTo(target, neighborSpring()) }
            }
        }
    }

    private fun rebound(neighbor: ControlButton, direction: Float) {
        val state = states.getValue(neighbor)
        scope.launch {
            state.offsetDp.animateTo(
                targetValue = 0f,
                animationSpec = neighborSpring(),
                // Added to the current velocity, so a bounce already in flight is not cut dead by a new tap.
                initialVelocity = state.offsetDp.velocity +
                    direction * springImpulseVelocity(REBOUND_OFFSET_PEAK_DP, NEIGHBOR_DAMPING, NEIGHBOR_STIFFNESS)
            )
        }
        scope.launch {
            state.scaleDelta.animateTo(
                targetValue = 0f,
                animationSpec = neighborSpring(SCALE_VISIBILITY_THRESHOLD),
                initialVelocity = state.scaleDelta.velocity +
                    springImpulseVelocity(REBOUND_SCALE_PEAK, NEIGHBOR_DAMPING, NEIGHBOR_STIFFNESS)
            )
        }
    }

    private fun neighborSpring(visibilityThreshold: Float = 0.01f) =
        spring(NEIGHBOR_DAMPING, NEIGHBOR_STIFFNESS, visibilityThreshold)
}

/**
 * Applies the appearance slide ([slideSign] is the side the button hides towards, `0f` for none) and the neighbors'
 * [state] in the draw phase only, so no frame of the animation recomposes.
 */
internal fun Modifier.playerControlMotion(
    slideSign: Float,
    slide: () -> Float,
    state: ButtonMotionState
): Modifier = graphicsLayer {
    translationX = slideSign * size.width * (1f - slide()) + state.offsetDp.value.dp.toPx()
    val scale = 1f + state.scaleDelta.value
    scaleX = scale
    scaleY = scale
}
