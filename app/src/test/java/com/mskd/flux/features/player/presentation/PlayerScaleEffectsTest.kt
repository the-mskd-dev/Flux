package com.mskd.flux.features.player.presentation

import androidx.compose.ui.unit.IntSize
import androidx.media3.common.VideoSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class PlayerScaleEffectsTest : FunSpec({

    val landscapePhone = IntSize(width = 2400, height = 1080)

    fun effects(videoSize: VideoSize, container: IntSize = landscapePhone, isPortrait: Boolean = false) =
        PlayerScaleEffects(videoSize = videoSize, containerSize = container, isPortrait = isPortrait)

    test("unknown video size has no display ratio and does not zoom") {
        VideoSize.UNKNOWN.displayAspectRatio.shouldBeNull()
        effects(VideoSize.UNKNOWN).fillScale shouldBe 1f
    }

    test("square pixels keep the coded ratio") {
        VideoSize(1440, 1080).displayAspectRatio shouldBe (4f / 3f plusOrMinus 0.0001f)
    }

    test("anamorphic pixels widen the display ratio") {
        // NTSC DVD widescreen: 720x480 coded, displayed 16:9
        VideoSize(720, 480, 32f / 27f).displayAspectRatio shouldBe (16f / 9f plusOrMinus 0.0001f)
    }

    test("4:3 picture on a wide screen fills by matching the screen width") {
        effects(VideoSize(1440, 1080)).fillScale shouldBe ((2400f / 1080f) / (4f / 3f) plusOrMinus 0.0001f)
    }

    test("anamorphic picture fills using its display ratio, not its coded size") {
        effects(VideoSize(720, 480, 32f / 27f)).fillScale shouldBe ((2400f / 1080f) / (16f / 9f) plusOrMinus 0.0001f)
    }

    test("pinch to fill zooms to the fill scale in landscape and back") {
        val effects = effects(VideoSize(1440, 1080))

        effects.toggleFill(true)
        effects.targetScale shouldBe effects.fillScale

        effects.toggleFill(false)
        effects.targetScale shouldBe 1f
    }

    test("pinch to fill does nothing in portrait") {
        val effects = effects(VideoSize(1920, 1080), container = IntSize(1080, 2400), isPortrait = true)

        effects.toggleFill(true)

        effects.targetScale shouldBe 1f
    }
})
