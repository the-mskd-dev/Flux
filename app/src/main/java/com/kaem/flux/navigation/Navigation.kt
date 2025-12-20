package com.kaem.flux.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import com.kaem.flux.model.artwork.ContentType
import kotlinx.serialization.Serializable

sealed class Route : NavKey {

    @Serializable
    data object Library: Route()

    @Serializable
    data class Artwork(val artworkId: Long): Route()

    @Serializable
    data class Search(val contentType: ContentType? = null): Route()

    @Serializable
    data object Settings: Route()

    @Serializable
    data object HowTo: Route()

    @Serializable
    data object About: Route()
}

object Transition {

    private val motionSpec = spring<Float>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    private val stiffness = spring<IntOffset>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    private fun enterFromRight() = slideInHorizontally(initialOffsetX = { it }, animationSpec = stiffness)

    private fun exitToLeft() = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = stiffness) +
            fadeOut(animationSpec = motionSpec) +
            scaleOut(targetScale = 0.9f, animationSpec = motionSpec)

    private fun enterFromLeft() = slideInHorizontally(initialOffsetX = { -it }, animationSpec = stiffness) +
            fadeIn(animationSpec = motionSpec) +
            scaleIn(initialScale = 0.9f, animationSpec = motionSpec)

    private fun exitToRight() = slideOutHorizontally(targetOffsetX = { it }, animationSpec = stiffness)

    val Forward = enterFromRight() togetherWith exitToLeft()
    val Backward = enterFromLeft() togetherWith exitToRight()

}