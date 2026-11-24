package com.mskd.flux.ui.components.buttons

import androidx.compose.runtime.MonotonicFrameClock
import com.mskd.flux.ui.components.ExpressiveRowMotion
import com.mskd.flux.ui.components.SideBounceNeighbors
import com.mskd.flux.ui.components.computeNetHoldPullDp
import com.mskd.flux.ui.components.springImpulseVelocity
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

    test("SideBounceNeighbors maps leftTag to -1f and rightTag to 1f") {
        SideBounceNeighbors(leftTag = "rewind", rightTag = "forward").entries() shouldBe listOf(
            "rewind" to -1f,
            "forward" to 1f
        )
        SideBounceNeighbors(rightTag = "play").entries() shouldBe listOf("play" to 1f)
        SideBounceNeighbors(leftTag = "play").entries() shouldBe listOf("play" to -1f)
        SideBounceNeighbors().entries() shouldBe emptyList()
    }

    test("a held source pulls its left neighbor right (+dp) and right neighbor left (-dp)") {
        val rewindHeld = listOf(SideBounceNeighbors(rightTag = "play"))
        computeNetHoldPullDp(
            "play",
            heldTags = emptySet(),
            heldSources = rewindHeld,
            pullDp = 2.5f
        ) shouldBe -2.5f

        val forwardHeld = listOf(SideBounceNeighbors(leftTag = "play"))
        computeNetHoldPullDp(
            "play",
            heldTags = emptySet(),
            heldSources = forwardHeld,
            pullDp = 2.5f
        ) shouldBe 2.5f

        val playHeld = listOf(SideBounceNeighbors(leftTag = "rewind", rightTag = "forward"))
        computeNetHoldPullDp(
            "rewind",
            heldTags = emptySet(),
            heldSources = playHeld,
            pullDp = 2.5f
        ) shouldBe 2.5f
        computeNetHoldPullDp(
            "forward",
            heldTags = emptySet(),
            heldSources = playHeld,
            pullDp = 2.5f
        ) shouldBe -2.5f
    }

    test("an element is not pulled by unrelated sources or when it is itself held") {
        val rewindHeld = listOf(SideBounceNeighbors(rightTag = "play"))
        computeNetHoldPullDp(
            "forward",
            heldTags = emptySet(),
            heldSources = rewindHeld,
            pullDp = 2.5f
        ) shouldBe 0f
        computeNetHoldPullDp(
            "play",
            heldTags = setOf("play"),
            heldSources = rewindHeld,
            pullDp = 2.5f
        ) shouldBe 0f
    }

    test("opposite pulls from both neighbors cancel out") {
        val bothSidesHeld = listOf(
            SideBounceNeighbors(rightTag = "play"),
            SideBounceNeighbors(leftTag = "play")
        )
        computeNetHoldPullDp(
            "play",
            heldTags = emptySet(),
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
            motion.retainTag("rewind")
            val playState = motion.stateFor("play")
            motion.retainTag("play")

            val source = Any()
            motion.onSourcePress(source, SideBounceNeighbors(rightTag = "play"))
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
            val playState = motion.stateFor("play")
            motion.retainTag("play")

            val source = Any()
            motion.onSourcePress(source, SideBounceNeighbors(rightTag = "play"))
            advanceTimeBy(80)
            runCurrent()
            playState.offsetDp.targetValue shouldBe -2.5f

            motion.onSourceCancel(source)
            advanceTimeBy(60)
            runCurrent()
            playState.offsetDp.targetValue shouldBe 0f
            playState.scaleDelta.value shouldBe 0f
            playState.scaleDelta.velocity shouldBe 0f
        }
    }

    test("multi-touch self-protection resets hold pull while tagged element is pressed") {
        withMotionScope { motion ->
            val playState = motion.stateFor("play")
            motion.retainTag("play")

            val source = Any()
            motion.onSourcePress(source, SideBounceNeighbors(rightTag = "play"))
            advanceTimeBy(80)
            runCurrent()
            playState.offsetDp.targetValue shouldBe -2.5f

            motion.onTagPress("play")
            runCurrent()
            playState.offsetDp.targetValue shouldBe 0f

            motion.onTagRelease("play")
            runCurrent()
            playState.offsetDp.targetValue shouldBe -2.5f

            motion.onSourceCancel(source)
        }
    }

    test("releasing a source targeting an unregistered tag does not throw") {
        withMotionScope { motion ->
            val source = Any()
            motion.onSourcePress(source,
                SideBounceNeighbors(leftTag = "missing-left", rightTag = "missing-right")
            )
            motion.onSourceRelease(source)
            advanceTimeBy(100)
            runCurrent()
        }
    }
})

@OptIn(ExperimentalCoroutinesApi::class)
private inline fun withMotionScope(block: TestScope.(ExpressiveRowMotion) -> Unit) {
    val scope = TestScope(TestMonotonicFrameClock())
    try {
        scope.block(ExpressiveRowMotion(scope.backgroundScope))
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
