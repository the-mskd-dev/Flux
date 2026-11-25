package com.mskd.flux.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.plus(
    top: Dp = 0.dp,
    bottom: Dp = Design.Space.large,
    horizontal: Dp = 0.dp,
    start: Dp = horizontal,
    end: Dp = horizontal,
    includeTop: Boolean = true,
    includeStart: Boolean = true,
    minBottom: Dp = 0.dp,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
): PaddingValues = PaddingValues(
    top = if (includeTop) calculateTopPadding() + top else top,
    bottom = maxOf(calculateBottomPadding(), minBottom) + bottom,
    start = if (includeStart) calculateStartPadding(layoutDirection) + start else start,
    end = calculateEndPadding(layoutDirection) + end,
)

@Composable
fun Modifier.scaffoldPadding(
    innerPadding: PaddingValues,
    top: Dp = 0.dp,
    bottom: Dp = Design.Space.large,
    horizontal: Dp = 0.dp,
    start: Dp = horizontal,
    end: Dp = horizontal,
    includeTop: Boolean = true,
    includeStart: Boolean = true,
    minBottom: Dp = 0.dp,
    layoutDirection: LayoutDirection = LocalLayoutDirection.current,
): Modifier = padding(
    innerPadding.plus(
        top = top,
        bottom = bottom,
        start = start,
        end = end,
        includeTop = includeTop,
        includeStart = includeStart,
        minBottom = minBottom,
        layoutDirection = layoutDirection,
    )
)
