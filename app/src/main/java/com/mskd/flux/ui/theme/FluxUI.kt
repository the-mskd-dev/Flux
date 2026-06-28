package com.mskd.flux.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

object FluxUI {

    val shapes: FluxShapes @Composable get() = LocalFluxShapes.current
    val itemsPerRow: FluxItemsPerRow @Composable get() = LocalItemsPerRow.current

}

data class FluxShapes(
    val cardCorner: Dp = 12.dp,
    val itemCorner: Dp = 8.dp,
)

data class FluxItemsPerRow(
    val artworks: Int = 3,
    val seasons: Int = 3
)

val LocalFluxShapes = compositionLocalOf { FluxShapes() }
val LocalItemsPerRow = compositionLocalOf { FluxItemsPerRow() }