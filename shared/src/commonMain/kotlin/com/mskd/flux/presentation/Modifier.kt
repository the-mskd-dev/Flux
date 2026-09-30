package com.mskd.flux.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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