package com.mskd.flux.ui.expressive

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.unit.IntSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent

@OptIn(ExperimentalCoroutinesApi::class)
class ExpressiveContentSizeStateTest : FunSpec({

    test("first animateTo returns initial size immediately with no animation") {
        withContentSizeState { state ->
            val initial = IntSize(100, 40)
            state.animateTo(initial) shouldBe initial
            advanceTimeBy(100)
            runCurrent()
            state.animateTo(initial) shouldBe initial
        }
    }

    test("growing overshoots target width and settles with finishedListener") {
        val finishedCalls = mutableListOf<Pair<IntSize, IntSize>>()
        withContentSizeState(
            finishedListener = { initial, target -> finishedCalls.add(initial to target) }
        ) { state ->
            val start = IntSize(100, 40)
            val target = IntSize(200, 40)
            state.animateTo(start) shouldBe start

            state.animateTo(target)
            var maxObservedWidth = start.width
            repeat(80) {
                advanceTimeBy(16)
                runCurrent()
                val current = state.animateTo(target)
                if (current.width > maxObservedWidth) {
                    maxObservedWidth = current.width
                }
            }

            maxObservedWidth shouldBeGreaterThan target.width
            state.animateTo(target) shouldBe target
            finishedCalls shouldBe listOf(start to target)
        }
    }

    test("shrinking undershoots target width and settles") {
        withContentSizeState { state ->
            val start = IntSize(200, 40)
            val target = IntSize(100, 40)
            state.animateTo(start) shouldBe start

            state.animateTo(target)
            var minObservedWidth = start.width
            repeat(80) {
                advanceTimeBy(16)
                runCurrent()
                val current = state.animateTo(target)
                if (current.width < minObservedWidth) {
                    minObservedWidth = current.width
                }
            }

            minObservedWidth shouldBeLessThan target.width
            state.animateTo(target) shouldBe target
        }
    }

    test("mid-flight target reversal settles at final target and invokes finishedListener once") {
        val finishedCalls = mutableListOf<Pair<IntSize, IntSize>>()
        withContentSizeState(
            finishedListener = { initial, target -> finishedCalls.add(initial to target) }
        ) { state ->
            val start = IntSize(100, 40)
            val intermediate = IntSize(220, 40)
            val finalTarget = IntSize(130, 40)

            state.animateTo(start) shouldBe start
            state.animateTo(intermediate)

            repeat(3) {
                advanceTimeBy(16)
                runCurrent()
            }

            state.animateTo(finalTarget)

            repeat(80) {
                advanceTimeBy(16)
                runCurrent()
            }

            state.animateTo(finalTarget) shouldBe finalTarget
            finishedCalls.size shouldBe 1
            finishedCalls.single().second shouldBe finalTarget
        }
    }

    test("reset causes next animateTo to snap immediately without animating") {
        withContentSizeState { state ->
            state.animateTo(IntSize(100, 40)) shouldBe IntSize(100, 40)
            state.reset()
            val newInitial = IntSize(240, 48)
            state.animateTo(newInitial) shouldBe newInitial
        }
    }
})

@OptIn(ExperimentalCoroutinesApi::class)
private inline fun withContentSizeState(
    noinline finishedListener: ((IntSize, IntSize) -> Unit)? = null,
    block: TestScope.(ExpressiveContentSizeState) -> Unit
) {
    val scope = TestScope(ContentSizeTestFrameClock())
    try {
        val state = ExpressiveContentSizeState(
            scope = scope.backgroundScope,
            finishedListener = finishedListener
        )
        scope.block(state)
    } finally {
        scope.cancel()
    }
}

private class ContentSizeTestFrameClock : MonotonicFrameClock {
    private var frameTimeNanos = 0L

    override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
        delay(16L)
        frameTimeNanos += 16_000_000L
        return onFrame(frameTimeNanos)
    }
}
