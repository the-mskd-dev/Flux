package com.mskd.flux.ui.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class FluxSpring(
    val dampingRatio: Float,
    val stiffness: Float
) {
    fun <T> toSpec(visibilityThreshold: T? = null): SpringSpec<T> = spring(
        dampingRatio = dampingRatio,
        stiffness = stiffness,
        visibilityThreshold = visibilityThreshold
    )
}

@Immutable
interface FluxMotionScheme : MotionScheme {
    val press: FluxSpring
    val contentSize: FluxSpring
    val neighborBounce: FluxSpring

    companion object {
        val Expressive: FluxMotionScheme = ExpressiveFluxMotionScheme
    }
}

private object ExpressiveFluxMotionScheme :
    FluxMotionScheme,
    MotionScheme by MotionScheme.expressive() {

    override val press = FluxSpring(dampingRatio = 0.35f, stiffness = 800f)
    override val contentSize = FluxSpring(dampingRatio = 0.5f, stiffness = 350f)
    override val neighborBounce = FluxSpring(dampingRatio = 0.48f, stiffness = 450f)
}

val LocalFluxMotionScheme: ProvidableCompositionLocal<FluxMotionScheme> =
    staticCompositionLocalOf { FluxMotionScheme.Expressive }

object Motion {

    val Expressive: FluxMotionScheme = FluxMotionScheme.Expressive

    val scheme: FluxMotionScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalFluxMotionScheme.current

    val press: FluxSpring
        @Composable
        @ReadOnlyComposable
        get() = scheme.press

    val contentSize: FluxSpring
        @Composable
        @ReadOnlyComposable
        get() = scheme.contentSize

    val neighborBounce: FluxSpring
        @Composable
        @ReadOnlyComposable
        get() = scheme.neighborBounce

    @Composable
    @ReadOnlyComposable
    fun <T> fastSpatial(): FiniteAnimationSpec<T> = scheme.fastSpatialSpec()

    @Composable
    @ReadOnlyComposable
    fun <T> defaultSpatial(): FiniteAnimationSpec<T> = scheme.defaultSpatialSpec()

    @Composable
    @ReadOnlyComposable
    fun <T> slowSpatial(): FiniteAnimationSpec<T> = scheme.slowSpatialSpec()

    @Composable
    @ReadOnlyComposable
    fun <T> fastEffects(): FiniteAnimationSpec<T> = scheme.fastEffectsSpec()

    @Composable
    @ReadOnlyComposable
    fun <T> defaultEffects(): FiniteAnimationSpec<T> = scheme.defaultEffectsSpec()

    @Composable
    @ReadOnlyComposable
    fun <T> slowEffects(): FiniteAnimationSpec<T> = scheme.slowEffectsSpec()

    @Composable
    fun buttonEnter(): EnterTransition =
        fadeIn(animationSpec = defaultEffects()) + scaleIn(animationSpec = fastSpatial())

    @Composable
    fun buttonExit(): ExitTransition =
        fadeOut(animationSpec = fastEffects()) + scaleOut(animationSpec = fastSpatial())
}
