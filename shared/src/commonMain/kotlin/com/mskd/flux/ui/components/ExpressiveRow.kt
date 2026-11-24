package com.mskd.flux.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.components.buttons.observeExpressivePress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

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

/** Neighbor tags targeted by a pressed element, paired with their outward direction (`-1f` left, `1f` right). */
data class SideBounceNeighbors(
    val leftTag: String? = null,
    val rightTag: String? = null
) {
    fun entries(): List<Pair<String, Float>> = buildList {
        if (leftTag != null) add(leftTag to -1f)
        if (rightTag != null) add(rightTag to 1f)
    }
}

/**
 * Horizontal offset [tag] takes while [heldSources] are pressed: it leans [pullDp] towards each held source
 * that targets it. An element currently in [heldTags] stays under the finger, and pulls from both sides cancel out.
 */
fun computeNetHoldPullDp(
    tag: String,
    heldTags: Set<String>,
    heldSources: Collection<SideBounceNeighbors>,
    pullDp: Float
): Float {
    if (tag in heldTags) return 0f
    return heldSources.sumOf { neighbors ->
        neighbors.entries()
            .filter { it.first == tag }
            .sumOf { (_, awayDirection) -> (-awayDirection * pullDp).toDouble() }
    }.toFloat()
}

/**
 * Initial velocity that makes an under-damped spring (mass 1) starting at rest reach [peak] at its first extreme.
 * From x(t) = v0 / wd * e^(-z*w*t) * sin(wd*t), whose first extreme is at tan(wd*t) = wd / (z*w).
 */
fun springImpulseVelocity(peak: Float, dampingRatio: Float, stiffness: Float): Float {
    val w = sqrt(stiffness)
    val decay = dampingRatio * w
    val wd = w * sqrt(1f - dampingRatio * dampingRatio)
    val tPeak = atan(wd / decay) / wd
    return peak * wd / (exp(-decay * tPeak) * sin(wd * tPeak))
}

/** Motion a tagged element receives when a neighbor is pressed. Read only during the draw phase. */
class ButtonMotionState {
    val offsetDp = Animatable(0f)
    val scaleDelta = Animatable(0f)
}

/**
 * Coordinates neighbor lean and rebound animations inside an [ExpressiveRow].
 *
 * Tagged elements register dynamically via [retainTag]/[releaseTag] and report their own press state via
 * [onTagPress]/[onTagRelease] so multi-touch never pulls an element currently under a finger.
 */
class ExpressiveRowMotion(
    private val scope: CoroutineScope
) {
    private val states = mutableMapOf<String, ButtonMotionState>()
    private val tagRefCounts = mutableMapOf<String, Int>()
    private val pressedTagCounts = mutableMapOf<String, Int>()
    private val activeSources = mutableMapOf<Any, SideBounceNeighbors>()
    private val heldSources = mutableMapOf<Any, SideBounceNeighbors>()
    private val holdJobs = mutableMapOf<Any, Job>()

    fun stateFor(tag: String): ButtonMotionState = states.getOrPut(tag) { ButtonMotionState() }

    fun retainTag(tag: String) {
        tagRefCounts[tag] = (tagRefCounts[tag] ?: 0) + 1
    }

    fun releaseTag(tag: String) {
        val next = (tagRefCounts[tag] ?: 0) - 1
        if (next <= 0) {
            tagRefCounts.remove(tag)
            states.remove(tag)
            if (pressedTagCounts.remove(tag) != null) applyHoldPull()
        } else {
            tagRefCounts[tag] = next
        }
    }

    fun onTagPress(tag: String) {
        val prev = pressedTagCounts[tag] ?: 0
        pressedTagCounts[tag] = prev + 1
        if (prev == 0) applyHoldPull()
    }

    fun onTagRelease(tag: String) {
        val next = (pressedTagCounts[tag] ?: 0) - 1
        if (next <= 0) {
            if (pressedTagCounts.remove(tag) != null) applyHoldPull()
        } else {
            pressedTagCounts[tag] = next
        }
    }

    fun onSourcePress(sourceId: Any, neighbors: SideBounceNeighbors) {
        holdJobs.remove(sourceId)?.cancel()
        activeSources[sourceId] = neighbors
        holdJobs[sourceId] = scope.launch {
            delay(HOLD_DELAY_MS)
            heldSources[sourceId] = neighbors
            applyHoldPull()
        }
    }

    fun onSourceRelease(sourceId: Any) {
        holdJobs.remove(sourceId)?.cancel()
        val neighbors = activeSources.remove(sourceId) ?: return
        if (heldSources.remove(sourceId) != null) applyHoldPull()
        scope.launch {
            delay(PROPAGATION_DELAY_MS)
            neighbors.entries().forEach { (tag, direction) -> rebound(tag, direction) }
        }
    }

    fun onSourceCancel(sourceId: Any) {
        holdJobs.remove(sourceId)?.cancel()
        activeSources.remove(sourceId)
        if (heldSources.remove(sourceId) != null) applyHoldPull()
    }

    private fun applyHoldPull() {
        val heldTags = pressedTagCounts.keys
        states.forEach { (tag, state) ->
            val target = computeNetHoldPullDp(tag, heldTags, heldSources.values, HOLD_PULL_DP)
            // Leave untouched elements alone: restarting their spring would cut a rebound still in flight.
            if (target != state.offsetDp.targetValue) {
                scope.launch { state.offsetDp.animateTo(target, neighborSpring()) }
            }
        }
    }

    private fun rebound(tag: String, direction: Float) {
        val state = states[tag] ?: return
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

/** Scope for children of [ExpressiveRow], providing [expressiveTag] and [expressiveSideBounce]. */
@LayoutScopeMarker
@Immutable
interface ExpressiveRowScope : RowScope {

    /** Registers this element under [tag] so neighbors can lean towards and bounce it when pressed. */
    @Composable
    fun Modifier.expressiveTag(tag: String): Modifier

    /**
     * Observes presses on this element and animates the elements tagged [leftTag] and [rightTag]
     * inside the enclosing [ExpressiveRow].
     */
    @Composable
    fun Modifier.expressiveSideBounce(
        leftTag: String? = null,
        rightTag: String? = null,
        enabled: Boolean = true,
        interactionSource: InteractionSource? = null
    ): Modifier
}

private class ExpressiveRowScopeInstance(
    rowScope: RowScope,
    private val motion: ExpressiveRowMotion
) : ExpressiveRowScope, RowScope by rowScope {

    @Composable
    override fun Modifier.expressiveTag(tag: String): Modifier {
        val state = remember(motion, tag) { motion.stateFor(tag) }

        DisposableEffect(motion, tag) {
            motion.retainTag(tag)
            onDispose { motion.releaseTag(tag) }
        }

        // Observe pointer before graphicsLayer so translation/scale never shifts hit bounds.
        return this
            .observeExpressivePress(
                enabled = true,
                interactionSource = null,
                onPress = { motion.onTagPress(tag) },
                onRelease = { motion.onTagRelease(tag) },
                onCancel = { motion.onTagRelease(tag) }
            )
            .graphicsLayer {
                translationX = state.offsetDp.value.dp.toPx()
                val scale = 1f + state.scaleDelta.value
                scaleX = scale
                scaleY = scale
            }
    }

    @Composable
    override fun Modifier.expressiveSideBounce(
        leftTag: String?,
        rightTag: String?,
        enabled: Boolean,
        interactionSource: InteractionSource?
    ): Modifier {
        if (leftTag == null && rightTag == null) return this
        val sourceId = remember { Any() }
        val neighbors = remember(leftTag, rightTag) { SideBounceNeighbors(leftTag, rightTag) }
        return this.observeExpressivePress(
            enabled = enabled,
            interactionSource = interactionSource,
            onPress = { motion.onSourcePress(sourceId, neighbors) },
            onRelease = { motion.onSourceRelease(sourceId) },
            onCancel = { motion.onSourceCancel(sourceId) }
        )
    }
}

/**
 * A [Row] whose children can coordinate neighbor lean and rebound animations via
 * [ExpressiveRowScope.expressiveTag] and [ExpressiveRowScope.expressiveSideBounce].
 */
@Composable
fun ExpressiveRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable ExpressiveRowScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val motion = remember(scope) { ExpressiveRowMotion(scope) }

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        val rowScope = remember(this, motion) { ExpressiveRowScopeInstance(this, motion) }
        rowScope.content()
    }
}
