package com.mskd.flux.ui.motion

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.IntSize
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class FluxMotionSchemeTest : FunSpec({

    test("FluxSpring.toSpec produces a SpringSpec with exact parameters and optional visibilityThreshold") {
        val spring = FluxSpring(dampingRatio = 0.42f, stiffness = 520f)

        val defaultSpec: SpringSpec<Float> = spring.toSpec()
        defaultSpec.dampingRatio shouldBe 0.42f
        defaultSpec.stiffness shouldBe 520f
        defaultSpec.visibilityThreshold shouldBe null

        val floatThresholdSpec: SpringSpec<Float> = spring.toSpec(visibilityThreshold = 0.001f)
        floatThresholdSpec.dampingRatio shouldBe 0.42f
        floatThresholdSpec.stiffness shouldBe 520f
        floatThresholdSpec.visibilityThreshold shouldBe 0.001f

        val intSizeThresholdSpec: SpringSpec<IntSize> =
            spring.toSpec(visibilityThreshold = IntSize.VisibilityThreshold)
        intSizeThresholdSpec.dampingRatio shouldBe 0.42f
        intSizeThresholdSpec.stiffness shouldBe 520f
        intSizeThresholdSpec.visibilityThreshold shouldBe IntSize.VisibilityThreshold
    }

    test("FluxMotionScheme.Expressive exposes expected custom spring tokens") {
        val scheme = FluxMotionScheme.Expressive

        Motion.Expressive shouldBe scheme
        scheme.press shouldBe FluxSpring(dampingRatio = 0.35f, stiffness = 800f)
        scheme.contentSize shouldBe FluxSpring(dampingRatio = 0.5f, stiffness = 350f)
        scheme.neighborBounce shouldBe FluxSpring(dampingRatio = 0.48f, stiffness = 450f)
    }

    test("FluxMotionScheme.Expressive delegates all 6 M3 specs identically to MotionScheme.expressive()") {
        val fluxScheme = FluxMotionScheme.Expressive
        val m3Expressive = MotionScheme.expressive()

        fluxScheme.fastSpatialSpec<Float>() shouldBe m3Expressive.fastSpatialSpec<Float>()
        fluxScheme.defaultSpatialSpec<Float>() shouldBe m3Expressive.defaultSpatialSpec<Float>()
        fluxScheme.slowSpatialSpec<Float>() shouldBe m3Expressive.slowSpatialSpec<Float>()
        fluxScheme.fastEffectsSpec<Float>() shouldBe m3Expressive.fastEffectsSpec<Float>()
        fluxScheme.defaultEffectsSpec<Float>() shouldBe m3Expressive.defaultEffectsSpec<Float>()
        fluxScheme.slowEffectsSpec<Float>() shouldBe m3Expressive.slowEffectsSpec<Float>()
    }
})
