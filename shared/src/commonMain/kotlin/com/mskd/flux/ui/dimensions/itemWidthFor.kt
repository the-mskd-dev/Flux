package com.mskd.flux.ui.dimensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import com.mskd.flux.ui.Design

fun itemWidthFor(
    screenWidthDp: Dp,
    columns: Int,
    horizontalPadding: Dp  = Design.Space.medium,
    spaceBy: Dp = Design.Space.small
) : Dp {
    val itemWidth = (screenWidthDp - horizontalPadding.times(2) - spaceBy.times(columns - 1)) / columns
    return min(itemWidth, Design.Dimension.itemWidth)
}