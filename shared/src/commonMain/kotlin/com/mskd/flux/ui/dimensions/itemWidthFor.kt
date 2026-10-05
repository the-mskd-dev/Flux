package com.mskd.flux.ui.dimensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import com.mskd.flux.ui.FluxUI

fun itemWidthFor(
    screenWidthDp: Dp,
    columns: Int,
    horizontalPadding: Dp  = FluxUI.Space.medium,
    spaceBy: Dp = FluxUI.Space.small
) : Dp {
    val itemWidth = (screenWidthDp - horizontalPadding.times(2) - spaceBy.times(columns - 1)) / columns
    return min(itemWidth, FluxUI.Dimension.itemWidth)
}