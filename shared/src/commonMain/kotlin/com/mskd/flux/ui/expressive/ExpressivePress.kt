package com.mskd.flux.ui.expressive

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput

/** Press feedback of expressive controls: a little more bounce and a slower return than the default press. */
const val PRESS_DAMPING = 0.35f
const val PRESS_STIFFNESS = 800f
const val PRESS_SCALE = 0.85f

/**
 * Observes press, release, and cancellation on this modifier without consuming pointer events.
 *
 * When [interactionSource] is provided, it takes precedence over raw pointer observation.
 * Otherwise, pointer events are observed non-destructively so any clickable/button works without
 * wiring a `MutableInteractionSource`.
 */
@Composable
internal fun Modifier.observeExpressivePress(
    enabled: Boolean = true,
    interactionSource: InteractionSource? = null,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onCancel: () -> Unit = onRelease
): Modifier {
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnCancel by rememberUpdatedState(onCancel)

    if (interactionSource != null) {
        LaunchedEffect(interactionSource, enabled) {
            if (!enabled) return@LaunchedEffect
            var isPressed = false
            try {
                interactionSource.interactions.collect { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> {
                            isPressed = true
                            currentOnPress()
                        }
                        is PressInteraction.Release -> {
                            isPressed = false
                            currentOnRelease()
                        }
                        is PressInteraction.Cancel -> {
                            isPressed = false
                            currentOnCancel()
                        }
                    }
                }
            } finally {
                if (isPressed) currentOnCancel()
            }
        }
        return this
    }

    if (!enabled) return this

    return this.pointerInput(Unit) {
        awaitEachGesture {
            // Wait on Final so the subsequent loop only sees Move/Up events, not the same down
            // event after clickable consumes it during Main.
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)

            var released = false
            currentOnPress()
            try {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) {
                        released = !change.isOutOfBounds(size, extendedTouchPadding)
                        break
                    }
                    if (change.isConsumed || change.isOutOfBounds(size, extendedTouchPadding)) {
                        break
                    }
                }
            } finally {
                if (released) currentOnRelease() else currentOnCancel()
            }
        }
    }
}

/**
 * Material 3 Expressive press feedback: the element squishes under the finger and bounces back on release.
 *
 * Works on any clickable/button automatically via non-consuming pointer observation; pass [interactionSource]
 * when you also want to react to non-pointer interactions (e.g. keyboard/D-pad) or share an explicit source.
 */
@Composable
fun Modifier.expressivePress(
    enabled: Boolean = true,
    interactionSource: InteractionSource? = null,
    pressedScale: Float = PRESS_SCALE,
    dampingRatio: Float = PRESS_DAMPING,
    stiffness: Float = PRESS_STIFFNESS
): Modifier {
    var isPressed by remember { mutableStateOf(false) }

    // Read in the draw phase only, so the animation does not recompose the caller on every frame.
    val scale = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        ),
        label = "ExpressivePress"
    )

    // Observe pointer before graphicsLayer so scaling down never shifts coordinates out of bounds.
    return this
        .observeExpressivePress(
            enabled = enabled,
            interactionSource = interactionSource,
            onPress = { isPressed = true },
            onRelease = { isPressed = false },
            onCancel = { isPressed = false }
        )
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
}
