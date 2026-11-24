package com.mskd.flux.ui.global

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.mskd.flux.ui.components.buttons.PRESS_DAMPING
import com.mskd.flux.ui.components.buttons.PRESS_SCALE
import com.mskd.flux.ui.components.buttons.PRESS_STIFFNESS

/**
 * Material 3 Expressive press feedback: the element squishes under the finger and bounces back on release.
 *
 * Pass the same [interactionSource] to the button, so the press it reacts to is the button's own.
 */
@Composable
fun Modifier.expressivePressScale(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = PRESS_SCALE,
    dampingRatio: Float = PRESS_DAMPING,
    stiffness: Float = PRESS_STIFFNESS
): Modifier {

    val isPressed by interactionSource.collectIsPressedAsState()

    // Read in the draw phase only, so the animation does not recompose the caller on every frame.
    val scale = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        ),
        label = "ExpressivePressScale"
    )

    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }

}
