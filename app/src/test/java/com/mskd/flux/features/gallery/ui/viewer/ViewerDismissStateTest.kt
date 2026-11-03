package com.mskd.flux.features.gallery.ui.viewer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe

class ViewerDismissStateTest : FunSpec({

    // 25 % of a 1000 px high viewer: the dismiss threshold is 250 px
    fun dismissState() = ViewerDismissState(flingVelocity = 2_000f).apply { height = 1_000f }

    test("a downward drag moves the image by the drag distance") {
        val state = dismissState()
        state.drag(100f)
        state.drag(25f)
        state.offset shouldBe 125f
    }

    test("the image cannot go above its resting position, and follows the finger back up") {
        val state = dismissState()
        state.drag(-50f)
        state.offset shouldBe 0f
        state.drag(200f)
        state.drag(-80f)
        state.offset shouldBe 120f
        state.drag(-500f)
        state.offset shouldBe 0f
    }

    test("the image shrinks and the background fades with the distance, down to 0.85 and transparent at the threshold") {
        val state = dismissState()
        state.scale shouldBe 1f
        state.backgroundAlpha shouldBe 1f

        state.drag(125f)
        state.scale shouldBe (0.925f plusOrMinus 0.001f)
        state.backgroundAlpha shouldBe (0.5f plusOrMinus 0.001f)

        state.drag(125f)
        state.scale shouldBe (0.85f plusOrMinus 0.001f)
        state.backgroundAlpha shouldBe (0f plusOrMinus 0.001f)

        state.drag(300f)
        state.scale shouldBe (0.85f plusOrMinus 0.001f)
        state.backgroundAlpha shouldBe (0f plusOrMinus 0.001f)
    }

    test("moving back up brings the scale and the background back") {
        val state = dismissState()
        state.drag(250f)
        state.drag(-200f)
        state.scale shouldBe (0.97f plusOrMinus 0.001f)
        state.backgroundAlpha shouldBe (0.8f plusOrMinus 0.001f)
    }

    test("a release past 25 % of the height dismisses the viewer") {
        val state = dismissState()
        state.drag(250f)
        state.release(velocity = 0f) shouldBe ViewerDismissRelease.DISMISS
    }

    test("a release before the threshold settles the image back") {
        val state = dismissState()
        state.drag(249f)
        state.release(velocity = 0f) shouldBe ViewerDismissRelease.SETTLE
    }

    test("a fast downward fling dismisses the viewer before the threshold") {
        val state = dismissState()
        state.drag(40f)
        state.release(velocity = 2_000f) shouldBe ViewerDismissRelease.DISMISS
    }

    test("a slow downward release or a fast upward fling settles the image back, even past the threshold") {
        val state = dismissState()
        state.drag(40f)
        state.release(velocity = 1_999f) shouldBe ViewerDismissRelease.SETTLE
        state.drag(300f)
        state.release(velocity = -2_000f) shouldBe ViewerDismissRelease.SETTLE
    }

})
