package com.mskd.flux.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object FluxUI {

    val shapes: Shapes @Composable get() = LocalUiShapes.current
    val global: Global @Composable get() = LocalUiGlobal.current
    val itemsPerRow: ItemsPerRow @Composable get() = LocalUiItemsPerRow.current
    val episodes: Episodes @Composable get() = LocalUiEpisodes.current
    val player: Player @Composable get() = LocalUiPlayer.current

    data class Shapes(
        val cardCorner: Dp = 12.dp,
        val itemCorner: Dp = 8.dp,
    )

    data class ItemsPerRow(
        val artworks: Int = 3,
        val seasons: Int = 3
    )

    data class Global(
        val oldBlurredHeader: Boolean = false,
    )

    data class Episodes(
        val large: Boolean = false
    )

    data class Player(
        val waveProgress: Boolean = true
    )

}

val LocalUiShapes = compositionLocalOf { FluxUI.Shapes() }
val LocalUiGlobal = compositionLocalOf { FluxUI.Global() }
val LocalUiItemsPerRow = compositionLocalOf { FluxUI.ItemsPerRow() }
val LocalUiEpisodes = compositionLocalOf { FluxUI.Episodes() }
val LocalUiPlayer = compositionLocalOf { FluxUI.Player() }