package com.mskd.flux.ui.expressive

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import com.mskd.flux.ui.motion.FluxSpring
import com.mskd.flux.ui.motion.LocalFluxMotionScheme
import com.mskd.flux.ui.motion.Motion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
