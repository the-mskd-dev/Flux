package com.mskd.flux.presentation

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.material3.Material3
import dev.chrisbanes.haze.hazeSource

@Composable
fun Modifier.blurBackground(
    state: HazeState,
    zIndex: Float = 0f,
    key: Any? = null
) = this.hazeSource(
    state = state,
    zIndex = zIndex,
    key = key
)

@Composable
fun Modifier.blurForeground(
    state: HazeState,
    alpha: Float = 1f,
    performanceMode: HazePerformanceMode? = null,
    expandLayerBounds: Boolean = true
) = this.hazeBlur(
    input = HazeInput.Sources(state),
    style = HazeBlurStyle.Material3 {
        alpha(alpha)
    },
    performanceMode = performanceMode,
    expandLayerBounds = expandLayerBounds
)

@Composable
fun Modifier.fillMaxWidthWithLimit(
    max: Dp = 500.dp,
    @FloatRange fraction: Float = 1f
) : Modifier {
    return this
        .widthIn(max = max)
        .fillMaxWidth(fraction = fraction)
}