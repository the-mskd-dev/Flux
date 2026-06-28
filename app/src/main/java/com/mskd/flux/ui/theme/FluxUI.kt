package com.mskd.flux.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

object FluxUI {

    val shapes: FluxShapes @Composable get() = LocalFluxShapes.current

}

data class FluxShapes(
    val cardCorner: Dp = 12.dp,
    val itemCorner: Dp = 8.dp,
)

val LocalFluxShapes = compositionLocalOf { FluxShapes() }