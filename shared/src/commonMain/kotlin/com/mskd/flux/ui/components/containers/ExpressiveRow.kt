package com.mskd.flux.ui.components.containers

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.expressive.observeExpressivePress
import com.mskd.flux.ui.motion.FluxSpring
import com.mskd.flux.ui.motion.Motion
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

private const val HOLD_PULL_DP = 1.5f

/** Outward peak reached by a neighbor when the tapped button rebounds, and its scale dip at that moment. */
private const val REBOUND_OFFSET_PEAK_DP = 4.5f
private const val REBOUND_SCALE_PEAK = -0.035f

/** Compose's default threshold (0.01) would cut the scale rebound (~0.016) short. */
private const val SCALE_VISIBILITY_THRESHOLD = 0.001f

/** Opaque handle identifying a child inside an [ExpressiveRow], created via [ExpressiveRowScope.createRef] or [ExpressiveRowScope.createRefs]. */
@Immutable
class ExpressiveRef

/** Destructurable bundle of [ExpressiveRef] handles returned by [ExpressiveRowScope.createRefs]. */
@Immutable
class ExpressiveRowRefs internal constructor(private val refs: List<ExpressiveRef>) {
    operator fun component1(): ExpressiveRef = refs[0]
    operator fun component2(): ExpressiveRef = refs[1]
    operator fun component3(): ExpressiveRef = refs[2]
    operator fun component4(): ExpressiveRef = refs[3]
    operator fun component5(): ExpressiveRef = refs[4]
    operator fun component6(): ExpressiveRef = refs[5]
    operator fun component7(): ExpressiveRef = refs[6]
    operator fun component8(): ExpressiveRef = refs[7]
    operator fun component9(): ExpressiveRef = refs[8]
    operator fun component10(): ExpressiveRef = refs[9]
    operator fun component11(): ExpressiveRef = refs[10]
    operator fun component12(): ExpressiveRef = refs[11]
    operator fun component13(): ExpressiveRef = refs[12]
    operator fun component14(): ExpressiveRef = refs[13]
    operator fun component15(): ExpressiveRef = refs[14]
    operator fun component16(): ExpressiveRef = refs[15]
}

/** Neighbor refs targeted by a pressed element, paired with their outward direction (`-1f` left, `1f` right). */
data class SideBounceNeighbors(
    val left: ExpressiveRef? = null,
    val right: ExpressiveRef? = null
) {
    fun entries(): List<Pair<ExpressiveRef, Float>> = buildList {
        if (left != null) add(left to -1f)
        if (right != null) add(right to 1f)
    }
}

/**
 * Horizontal offset [ref] takes while [heldSources] are pressed: it leans [pullDp] towards each held source
 * that targets it. An element currently in [heldRefs] stays under the finger, and pulls from both sides cancel out.
 */
fun computeNetHoldPullDp(
    ref: ExpressiveRef,
    heldRefs: Set<ExpressiveRef>,
    heldSources: Collection<SideBounceNeighbors>,
    pullDp: Float
): Float {
    if (ref in heldRefs) return 0f
    return heldSources.sumOf { neighbors ->
        neighbors.entries()
            .filter { it.first == ref }
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

/** Motion a referenced element receives when a neighbor is pressed. Read only during the draw phase. */
class ButtonMotionState {
    val offsetDp = Animatable(0f)
    val scaleDelta = Animatable(0f)
}

/**
 * Coordinates neighbor lean and rebound animations inside an [ExpressiveRow].
 *
 * Referenced elements register dynamically via [retainRef]/[releaseRef] and report their own press state via
 * [onRefPress]/[onRefRelease] so multi-touch never pulls an element currently under a finger.
 */
class ExpressiveRowMotion(
    private val scope: CoroutineScope,
    var neighborSpring: FluxSpring = Motion.Expressive.neighborBounce
) {
    private val states = mutableMapOf<ExpressiveRef, ButtonMotionState>()
    private val refCounts = mutableMapOf<ExpressiveRef, Int>()
    private val pressedRefCounts = mutableMapOf<ExpressiveRef, Int>()
    private val activeSources = mutableMapOf<Any, SideBounceNeighbors>()
    private val heldSources = mutableMapOf<Any, SideBounceNeighbors>()
    private val holdJobs = mutableMapOf<Any, Job>()

    fun stateFor(ref: ExpressiveRef): ButtonMotionState = states.getOrPut(ref) { ButtonMotionState() }

    fun retainRef(ref: ExpressiveRef) {
        refCounts[ref] = (refCounts[ref] ?: 0) + 1
    }

    fun releaseRef(ref: ExpressiveRef) {
        val next = (refCounts[ref] ?: 0) - 1
        if (next <= 0) {
            refCounts.remove(ref)
            states.remove(ref)
            if (pressedRefCounts.remove(ref) != null) applyHoldPull()
        } else {
            refCounts[ref] = next
        }
    }

    fun onRefPress(ref: ExpressiveRef) {
        val prev = pressedRefCounts[ref] ?: 0
        pressedRefCounts[ref] = prev + 1
        if (prev == 0) applyHoldPull()
    }

    fun onRefRelease(ref: ExpressiveRef) {
        val next = (pressedRefCounts[ref] ?: 0) - 1
        if (next <= 0) {
            if (pressedRefCounts.remove(ref) != null) applyHoldPull()
        } else {
            pressedRefCounts[ref] = next
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
            neighbors.entries().forEach { (ref, direction) -> rebound(ref, direction) }
        }
    }

    fun onSourceCancel(sourceId: Any) {
        holdJobs.remove(sourceId)?.cancel()
        activeSources.remove(sourceId)
        if (heldSources.remove(sourceId) != null) applyHoldPull()
    }

    private fun applyHoldPull() {
        val heldRefs = pressedRefCounts.keys
        states.forEach { (ref, state) ->
            val target = computeNetHoldPullDp(ref, heldRefs, heldSources.values, HOLD_PULL_DP)
            // Leave untouched elements alone: restarting their spring would cut a rebound still in flight.
            if (target != state.offsetDp.targetValue) {
                scope.launch { state.offsetDp.animateTo(target, neighborSpring.toSpec()) }
            }
        }
    }

    private fun rebound(ref: ExpressiveRef, direction: Float) {
        val state = states[ref] ?: return
        val spring = neighborSpring
        scope.launch {
            state.offsetDp.animateTo(
                targetValue = 0f,
                animationSpec = spring.toSpec(),
                // Added to the current velocity, so a bounce already in flight is not cut dead by a new tap.
                initialVelocity = state.offsetDp.velocity +
                    direction * springImpulseVelocity(REBOUND_OFFSET_PEAK_DP, spring.dampingRatio, spring.stiffness)
            )
        }
        scope.launch {
            state.scaleDelta.animateTo(
                targetValue = 0f,
                animationSpec = spring.toSpec(visibilityThreshold = SCALE_VISIBILITY_THRESHOLD),
                initialVelocity = state.scaleDelta.velocity +
                    springImpulseVelocity(REBOUND_SCALE_PEAK, spring.dampingRatio, spring.stiffness)
            )
        }
    }
}

/** Scope for children of [ExpressiveRow], providing [createRef], [createRefs], and [expressiveSideBounce]. */
@LayoutScopeMarker
@Immutable
interface ExpressiveRowScope : RowScope {

    /** Creates a single remembered [ExpressiveRef] scoped to this [ExpressiveRow]. */
    @Composable
    fun createRef(): ExpressiveRef

    /** Creates a destructurable bundle of remembered [ExpressiveRef] handles scoped to this [ExpressiveRow]. */
    @Composable
    fun createRefs(): ExpressiveRowRefs

    /**
     * Participates in neighbor bounce animations inside the enclosing [ExpressiveRow]:
     * - Pass [ref] to let neighbors lean towards and bounce this element.
     * - Pass [left] and/or [right] to push those neighbors when this element is pressed and released.
     */
    @Composable
    fun Modifier.expressiveSideBounce(
        ref: ExpressiveRef? = null,
        left: ExpressiveRef? = null,
        right: ExpressiveRef? = null,
        enabled: Boolean = true,
        interactionSource: InteractionSource? = null
    ): Modifier
}

private class ExpressiveRowScopeInstance(
    rowScope: RowScope,
    private val motion: ExpressiveRowMotion
) : ExpressiveRowScope, RowScope by rowScope {

    @Composable
    override fun createRef(): ExpressiveRef = remember { ExpressiveRef() }

    @Composable
    override fun createRefs(): ExpressiveRowRefs = remember { ExpressiveRowRefs(List(16) { ExpressiveRef() }) }

    @Composable
    override fun Modifier.expressiveSideBounce(
        ref: ExpressiveRef?,
        left: ExpressiveRef?,
        right: ExpressiveRef?,
        enabled: Boolean,
        interactionSource: InteractionSource?
    ): Modifier {
        if (ref == null && left == null && right == null) return this

        val state = if (ref != null) {
            DisposableEffect(motion, ref) {
                motion.retainRef(ref)
                onDispose { motion.releaseRef(ref) }
            }
            remember(motion, ref) { motion.stateFor(ref) }
        } else {
            null
        }

        val hasNeighbors = left != null || right != null
        val sourceId = remember { Any() }
        val neighbors = remember(left, right) { SideBounceNeighbors(left, right) }

        // Observe pointer before graphicsLayer so translation/scale never shifts hit bounds.
        val observed = this.observeExpressivePress(
            enabled = enabled || ref != null,
            interactionSource = interactionSource,
            onPress = {
                if (ref != null) motion.onRefPress(ref)
                if (enabled && hasNeighbors) motion.onSourcePress(sourceId, neighbors)
            },
            onRelease = {
                if (ref != null) motion.onRefRelease(ref)
                if (enabled && hasNeighbors) motion.onSourceRelease(sourceId)
            },
            onCancel = {
                if (ref != null) motion.onRefRelease(ref)
                if (enabled && hasNeighbors) motion.onSourceCancel(sourceId)
            }
        )

        return if (state != null) {
            observed.graphicsLayer {
                translationX = state.offsetDp.value.dp.toPx()
                val scale = 1f + state.scaleDelta.value
                scaleX = scale
                scaleY = scale
            }
        } else {
            observed
        }
    }
}

/**
 * A [Row] whose children can coordinate neighbor lean and rebound animations via
 * [ExpressiveRowScope.createRefs] and [ExpressiveRowScope.expressiveSideBounce].
 */
@Composable
fun ExpressiveRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable ExpressiveRowScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val neighborSpring = Motion.neighborBounce
    val motion = remember(scope) { ExpressiveRowMotion(scope, neighborSpring) }
    SideEffect { motion.neighborSpring = neighborSpring }

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment
    ) {
        val rowScope = remember(this, motion) { ExpressiveRowScopeInstance(this, motion) }
        rowScope.content()
    }
}
