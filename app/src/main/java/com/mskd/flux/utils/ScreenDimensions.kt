package com.mskd.flux.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.ui.theme.Ui


data class ScreenDimensions(
    val widthDp: Dp,
    val isLarge: Boolean
)

@Composable
fun rememberScreenDimensions(): ScreenDimensions {

    val widthPx = LocalWindowInfo.current.containerSize.width
    val widthDp = with(LocalDensity.current) { widthPx.toDp() }

    val isLarge = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
    return remember(widthPx) {
        ScreenDimensions(
            widthDp = widthDp,
            isLarge =  isLarge
        )
    }
}

@Composable
fun itemWidthFor(
    columns: Int,
    horizontalPadding: Dp  = Ui.Space.MEDIUM,
    spaceBy: Dp = Ui.Space.SMALL
) : Dp {
    val screenDimensions = rememberScreenDimensions()
    return (screenDimensions.widthDp - horizontalPadding.times(2) - spaceBy.times(columns - 1)) / columns
}