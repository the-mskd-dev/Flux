package com.mskd.flux.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
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

    object Dimension {
        val itemWidth = 200.dp
        val itemRatio = 2f/3f
    }

    object Images {

        const val ratio_1_1 = 1f
        const val ratio_2_3 = 2f/3f
        const val ratio_6_5 = 6f/5f
        const val ratio_5_6 = 5f/6f
        const val ratio_16_9 = 16f/9f

    }

    object Space {
        val listItem = 2.dp
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val bottomScreen = 100.dp
    }

    object Animation {

        val buttonEnter = fadeIn() + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        val buttonExit = fadeOut() + scaleOut()

    }

    object Grid {
        val startPadding = PaddingValues(start = Space.medium)
        val lastPadding = PaddingValues(end = Space.medium)
        val middlePadding = PaddingValues(horizontal = Space.small)
    }

    object Elevation {
        val itemShadow = 1.dp
    }

    data class Shapes(
        val corners: RoundedCornerShape = RoundedCornerShape(12.dp),
        val listItem: Dp = 16.dp
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