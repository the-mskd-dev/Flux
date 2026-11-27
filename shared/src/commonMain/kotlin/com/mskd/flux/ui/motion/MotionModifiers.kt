package com.mskd.flux.ui.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidatePlacement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.constrain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Press scale of expressive controls when held down. */
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
    spring: FluxSpring = Motion.press
): Modifier {

    var isPressed by remember { mutableStateOf(false) }

    // Read in the draw phase only, so the animation does not recompose the caller on every frame.
    val scale = animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring.toSpec(),
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

/**
 * Plays a squish-and-release bounce whenever [state] changes after initial composition.
 *
 * Read in the draw phase only via [graphicsLayer], so the animation does not recompose the caller.
 */
@Composable
fun Modifier.expressiveBounceOnChange(
    state: Any?,
    pressedScale: Float = PRESS_SCALE,
    animationSpec: FiniteAnimationSpec<Float> = Motion.slowEffects()
): Modifier {
    val bounceScale = remember { Animatable(1f) }
    var previousState by remember { mutableStateOf(state) }
    val currentSpec by rememberUpdatedState(animationSpec)
    val currentPressedScale by rememberUpdatedState(pressedScale)

    LaunchedEffect(state) {
        if (state == previousState) return@LaunchedEffect
        previousState = state
        bounceScale.snapTo(currentPressedScale)
        bounceScale.animateTo(1f, animationSpec = currentSpec)
    }

    return this.graphicsLayer {
        scaleX = bounceScale.value
        scaleY = bounceScale.value
    }
}

private val InvalidSize = IntSize(Int.MIN_VALUE, Int.MIN_VALUE)

/**
 * Drives spring-based size transitions for [expressiveContentSize].
 *
 * The first [animateTo] call snaps immediately without animating; subsequent size changes launch
 * a spring animation on [scope] and return the current animated [IntSize] read from snapshot state.
 */
class ExpressiveContentSizeState(
    private val scope: CoroutineScope,
    var spring: FluxSpring = Motion.Expressive.contentSize,
    var finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)? = null
) {
    private var anim: Animatable<IntSize, AnimationVector2D>? = null
    private var currentTarget: IntSize = InvalidSize

    fun animateTo(targetSize: IntSize): IntSize {
        val existing = anim
        if (existing == null) {
            anim = Animatable(targetSize, IntSize.VectorConverter, IntSize.VisibilityThreshold)
            currentTarget = targetSize
            return targetSize
        }

        if (targetSize != currentTarget) {
            val startSize = existing.value
            // Update synchronously so repeated measure passes before the coroutine starts don't re-launch.
            currentTarget = targetSize
            val spec = spring.toSpec(visibilityThreshold = IntSize.VisibilityThreshold)
            scope.launch {
                val result = existing.animateTo(
                    targetValue = targetSize,
                    animationSpec = spec
                )
                if (result.endReason == AnimationEndReason.Finished) {
                    finishedListener?.invoke(startSize, result.endState.value)
                }
            }
        }

        return existing.value
    }

    fun reset() {
        anim = null
        currentTarget = InvalidSize
    }
}

/**
 * Animates this layout's size whenever its measured content size changes, using a bouncy spring.
 *
 * Unlike `Modifier.animateContentSize()`, clipping is disabled by default (`clip = false`) so
 * containers such as pill buttons can stretch and undershoot smoothly without clipping inner content.
 */
fun Modifier.expressiveContentSize(
    spring: FluxSpring? = null,
    alignment: Alignment = Alignment.Center,
    clip: Boolean = false,
    finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)? = null
): Modifier = (if (clip) this.clipToBounds() else this) then
    ExpressiveContentSizeElement(
        spring = spring,
        alignment = alignment,
        finishedListener = finishedListener
    )

private data class ExpressiveContentSizeElement(
    val spring: FluxSpring?,
    val alignment: Alignment,
    val finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)?
) : ModifierNodeElement<ExpressiveContentSizeNode>() {

    override fun create(): ExpressiveContentSizeNode = ExpressiveContentSizeNode(
        spring = spring,
        alignment = alignment,
        finishedListener = finishedListener
    )

    override fun update(node: ExpressiveContentSizeNode) {
        val alignmentChanged = node.alignment != alignment
        node.spring = spring
        node.alignment = alignment
        node.finishedListener = finishedListener
        node.state?.let {
            if (node.isAttached) {
                it.spring = node.resolveSpring()
            } else if (spring != null) {
                it.spring = spring
            }
            it.finishedListener = finishedListener
        }
        if (alignmentChanged) {
            node.invalidatePlacement()
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "expressiveContentSize"
        properties["spring"] = spring
        properties["alignment"] = alignment
    }
}

private class ExpressiveContentSizeNode(
    var spring: FluxSpring?,
    var alignment: Alignment,
    var finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)?
) : LayoutModifierNode, CompositionLocalConsumerModifierNode, Modifier.Node() {

    var state: ExpressiveContentSizeState? = null

    fun resolveSpring(): FluxSpring = spring ?: currentValueOf(LocalFluxMotionScheme).contentSize

    override fun onReset() {
        state = null
    }

    override fun onDetach() {
        state = null
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int = measurable.minIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int = measurable.minIntrinsicHeight(width)

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ): Int = measurable.maxIntrinsicWidth(height)

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ): Int = measurable.maxIntrinsicHeight(width)

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        val measuredSize = IntSize(placeable.width, placeable.height)

        if (isLookingAhead) {
            return layout(measuredSize.width, measuredSize.height) {
                placeable.place(0, 0)
            }
        }

        val resolvedSpring = resolveSpring()
        val animState = state?.also { it.spring = resolvedSpring }
            ?: ExpressiveContentSizeState(
                scope = coroutineScope,
                spring = resolvedSpring,
                finishedListener = finishedListener
            ).also { state = it }

        val animatedSize = constraints.constrain(animState.animateTo(measuredSize))

        return layout(animatedSize.width, animatedSize.height) {
            val offset = alignment.align(
                size = measuredSize,
                space = animatedSize,
                layoutDirection = layoutDirection
            )
            // alignment.align already accounts for layoutDirection.
            placeable.place(offset)
        }
    }
}
