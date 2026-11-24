package com.mskd.flux.screens.player.composables.playerInterface

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeLessThan
import io.kotest.matchers.shouldBe

class PlayerControlButtonsMotionTest : FunSpec({

    test("tapping rewind or forward pushes only play, away from the tapped button") {
        ControlButton.REWIND.directNeighbors() shouldBe listOf(ControlButton.PLAY to 1f)
        ControlButton.FORWARD.directNeighbors() shouldBe listOf(ControlButton.PLAY to -1f)
    }

    test("a held button pulls its direct neighbors towards it") {
        computeNetHoldPullDp(ControlButton.PLAY, held = setOf(ControlButton.REWIND), pullDp = 2.5f) shouldBe -2.5f
        computeNetHoldPullDp(ControlButton.PLAY, held = setOf(ControlButton.FORWARD), pullDp = 2.5f) shouldBe 2.5f
        computeNetHoldPullDp(ControlButton.REWIND, held = setOf(ControlButton.PLAY), pullDp = 2.5f) shouldBe 2.5f
        computeNetHoldPullDp(ControlButton.FORWARD, held = setOf(ControlButton.PLAY), pullDp = 2.5f) shouldBe -2.5f
    }

    test("a button is not pulled by itself nor by a button that is not next to it") {
        computeNetHoldPullDp(ControlButton.PLAY, held = setOf(ControlButton.PLAY), pullDp = 2.5f) shouldBe 0f
        computeNetHoldPullDp(ControlButton.REWIND, held = setOf(ControlButton.FORWARD), pullDp = 2.5f) shouldBe 0f
    }

    test("play is not pulled when rewind and forward are held together") {
        val held = setOf(ControlButton.REWIND, ControlButton.FORWARD)
        computeNetHoldPullDp(ControlButton.PLAY, held, pullDp = 2.5f) shouldBe 0f
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

    test("tapping play pushes rewind to the left and forward to the right") {
        ControlButton.PLAY.directNeighbors() shouldBe listOf(
            ControlButton.REWIND to -1f,
            ControlButton.FORWARD to 1f
        )
    }
})
