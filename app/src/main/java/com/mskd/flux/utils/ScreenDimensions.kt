package com.mskd.flux.utils

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mskd.flux.ui.theme.FluxUI


data class ScreenDimensions(
    val widthDp: Dp,
    val isLarge: Boolean
)

@Composable
fun rememberScreenDimensions(): ScreenDimensions {

    val widthPx = LocalWindowInfo.current.containerSize.width
    val widthDp2 = LocalConfiguration.current.screenWidthDp.dp
    val widthDp = with(LocalDensity.current) { widthPx.toDp() }
    val isLarge = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    return ScreenDimensions(
        widthDp = widthDp,
        isLarge =  isLarge
    )
}

fun itemWidthFor(
    screenWidthDp: Dp,
    columns: Int,
    horizontalPadding: Dp  = FluxUI.Space.medium,
    spaceBy: Dp = FluxUI.Space.small
) : Dp {
    return (screenWidthDp - horizontalPadding.times(2) - spaceBy.times(columns - 1)) / columns
}