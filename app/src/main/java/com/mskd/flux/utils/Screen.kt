package com.mskd.flux.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

object Screen {

    @Composable
    fun isLargeScreen() : Boolean {
        val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
        return windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    }

}