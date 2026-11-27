package com.mskd.flux.ui.expressive

import androidx.compose.runtime.MonotonicFrameClock
import com.mskd.flux.ui.components.containers.ExpressiveRef
import com.mskd.flux.ui.components.containers.ExpressiveRowMotion
import com.mskd.flux.ui.components.containers.SideBounceNeighbors
import com.mskd.flux.ui.components.containers.computeNetHoldPullDp
import com.mskd.flux.ui.components.containers.springImpulseVelocity
import com.mskd.flux.ui.motion.FluxSpring
import com.mskd.flux.ui.motion.Motion
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

@OptIn(ExperimentalCoroutinesApi::class)
class ExpressiveRowMotionTest : FunSpec({

    test("SideBounceNeighbors maps left to -1f and right to 1f") {
        val rewind = ExpressiveRef()
        val play = ExpressiveRef()
        val forward = ExpressiveRef()

        SideBounceNeighbors(left = rewind, right = forward).entries() shouldBe listOf(
            rewind to -1f,
            forward to 1f
        )
        SideBounceNeighbors(right = play).entries() shouldBe listOf(play to 1f)
        SideBounceNeighbors(left = play).entries() shouldBe listOf(play to -1f)
        SideBounceNeighbors().entries() shouldBe emptyList()
    }

    test("a held source pulls its left neighbor right (+dp) and right neighbor left (-dp)") {
        val rewind = ExpressiveRef()
        val play = ExpressiveRef()
        val forward = ExpressiveRef()

        val rewindHeld = listOf(SideBounceNeighbors(right = play))
        computeNetHoldPullDp(
            play,
            heldRefs = emptySet(),
            heldSources = rewindHeld,
            pullDp = 2.5f
        ) shouldBe -2.5f

        val forwardHeld = listOf(SideBounceNeighbors(left = play))
        computeNetHoldPullDp(
            play,
            heldRefs = emptySet(),
            heldSources = forwardHeld,
            pullDp = 2.5f
        ) shouldBe 2.5f

        val playHeld = listOf(SideBounceNeighbors(left = rewind, right = forward))
        computeNetHoldPullDp(
            rewind,
            heldRefs = emptySet(),
            heldSources = playHeld,
            pullDp = 2.5f
        ) shouldBe 2.5f
        computeNetHoldPullDp(
            forward,
            heldRefs = emptySet(),
            heldSources = playHeld,
            pullDp = 2.5f
        ) shouldBe -2.5f
    }

    test("an element is not pulled by unrelated sources or when it is itself held") {
        val play = ExpressiveRef()
        val forward = ExpressiveRef()

        val rewindHeld = listOf(SideBounceNeighbors(right = play))
        computeNetHoldPullDp(
            forward,
            heldRefs = emptySet(),
            heldSources = rewindHeld,
            pullDp = 2.5f
        ) shouldBe 0f
        computeNetHoldPullDp(
            play,
            heldRefs = setOf(play),
            heldSources = rewindHeld,
            pullDp = 2.5f
        ) shouldBe 0f
    }

    test("opposite pulls from both neighbors cancel out") {
        val play = ExpressiveRef()
        val bothSidesHeld = listOf(
            SideBounceNeighbors(right = play),
            SideBounceNeighbors(left = play)
        )
        computeNetHoldPullDp(
            play,
            heldRefs = emptySet(),
            heldSources = bothSidesHeld,
            pullDp = 2.5f
        ) shouldBe 0f
    }

    // Independent check: integrate the spring step by step and read the extreme it reaches from rest.
    fun simulatedExtreme(velocity: Float, dampingRatio: Float, stiffness: Float): Float {
        val dt = 0.00005f
        val damping = 2f * dampingRatio * kotlin.math.sqrt(stiffness)
        var x = 0f
        var v = velocity
        var extreme = 0f
        repeat(40_000) {
            v += (-stiffness * x - damping * v) * dt
            x += v * dt
            if (kotlin.math.abs(x) > kotlin.math.abs(extreme)) extreme = x
        }
        return extreme
    }

    test("impulse velocity makes the spring reach the requested outward peak") {
        val velocity = springImpulseVelocity(peak = 8f, dampingRatio = 0.38f, stiffness = 450f)
        simulatedExtreme(velocity, 0.38f, 450f) shouldBe (8f plusOrMinus 0.05f)
    }

    test("impulse velocity is negative for a negative peak") {
        val velocity = springImpulseVelocity(peak = -0.06f, dampingRatio = 0.38f, stiffness = 450f)
        velocity shouldBeLessThan 0f
        simulatedExtreme(velocity, 0.38f, 450f) shouldBe (-0.06f plusOrMinus 0.0005f)
    }

    test("quick tap skips hold pull and triggers rebound after propagation delay") {
        withMotionScope { motion ->
            val rewind = ExpressiveRef()
            val play = ExpressiveRef()
            motion.retainRef(rewind)
            val playState = motion.stateFor(play)
            motion.retainRef(play)

            val source = Any()
            motion.onSourcePress(source, SideBounceNeighbors(right = play))
            advanceTimeBy(30)
            runCurrent()
            playState.offsetDp.targetValue shouldBe 0f

            motion.onSourceRelease(source)
            advanceTimeBy(50)
            runCurrent()
            playState.scaleDelta.velocity shouldBeLessThan 0f
            (playState.offsetDp.velocity > 0f) shouldBe true
        }
    }

    test("hold pulls neighbor and cancel returns without rebound impulse") {
        withMotionScope { motion ->
            val play = ExpressiveRef()
            val playState = motion.stateFor(play)
            motion.retainRef(play)

            val source = Any()
            motion.onSourcePress(source, SideBounceNeighbors(right = play))
            advanceTimeBy(80)
            runCurrent()
            playState.offsetDp.targetValue shouldBe -1.5f

            motion.onSourceCancel(source)
            advanceTimeBy(60)
            runCurrent()
            playState.offsetDp.targetValue shouldBe 0f
            playState.scaleDelta.value shouldBe 0f
            playState.scaleDelta.velocity shouldBe 0f
        }
    }

    test("multi-touch self-protection resets hold pull while referenced element is pressed") {
        withMotionScope { motion ->
            val play = ExpressiveRef()
            val playState = motion.stateFor(play)
            motion.retainRef(play)

            val source = Any()
            motion.onSourcePress(source, SideBounceNeighbors(right = play))
            advanceTimeBy(80)
            runCurrent()
            playState.offsetDp.targetValue shouldBe -1.5f

            motion.onRefPress(play)
            runCurrent()
            playState.offsetDp.targetValue shouldBe 0f

            motion.onRefRelease(play)
            runCurrent()
            playState.offsetDp.targetValue shouldBe -1.5f

            motion.onSourceCancel(source)
        }
    }

    test("releasing a source targeting an unregistered ref does not throw") {
        withMotionScope { motion ->
            val missingLeft = ExpressiveRef()
            val missingRight = ExpressiveRef()
            val source = Any()
            motion.onSourcePress(source,
                SideBounceNeighbors(left = missingLeft, right = missingRight)
            )
            motion.onSourceRelease(source)
            advanceTimeBy(100)
            runCurrent()
        }
    }

    test("custom neighborSpring drives rebound velocity calculation with its parameters") {
        val customSpring = FluxSpring(dampingRatio = 0.25f, stiffness = 900f)

        fun measureReboundVelocity(spring: FluxSpring): Float {
            var velocity = 0f
            withMotionScope(neighborSpring = spring) { motion ->
                val play = ExpressiveRef()
                val playState = motion.stateFor(play)
                motion.retainRef(play)

                val source = Any()
                motion.onSourcePress(source, SideBounceNeighbors(right = play))
                advanceTimeBy(30)
                runCurrent()
                motion.onSourceRelease(source)
                advanceTimeBy(50)
                runCurrent()

                velocity = playState.offsetDp.velocity
            }
            return velocity
        }

        val customVelocity = measureReboundVelocity(customSpring)
        val defaultVelocity = measureReboundVelocity(Motion.Expressive.neighborBounce)

        simulatedExtreme(customVelocity, customSpring.dampingRatio, customSpring.stiffness) shouldBe
            (4.5f plusOrMinus 0.05f)
        (customVelocity != defaultVelocity) shouldBe true
    }
})

@OptIn(ExperimentalCoroutinesApi::class)
private inline fun withMotionScope(
    neighborSpring: FluxSpring = Motion.Expressive.neighborBounce,
    block: TestScope.(ExpressiveRowMotion) -> Unit
) {
    val scope = TestScope(TestMonotonicFrameClock())
    try {
        scope.block(ExpressiveRowMotion(scope.backgroundScope, neighborSpring))
    } finally {
        scope.cancel()
    }
}

private class TestMonotonicFrameClock : MonotonicFrameClock {
    private var frameTimeNanos = 0L

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        delay(16L)
        frameTimeNanos += 16_000_000L
        return onFrame(frameTimeNanos)
    }
}
