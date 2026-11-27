package com.mskd.flux.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.customization.domain.model.NavigationStyle

object Design {

    val shapes: Shapes @Composable get() = LocalUiShapes.current
    val global: Global @Composable get() = LocalUiGlobal.current
    val itemsPerRow: ItemsPerRow @Composable get() = LocalUiItemsPerRow.current
    val episodes: Episodes @Composable get() = LocalUiEpisodes.current
    val player: Player @Composable get() = LocalUiPlayer.current

    object Dimension {
        val itemWidth = 200.dp
        const val itemRatio = 2f/3f
    }

    object Ratio {

        const val square = 1f
        const val imax = 1.43f/1f
        const val rectangle = 6f/5f
        const val landscape = 16f/9f

    }

    object Space {
        val listItem = 2.dp
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
    }

    object Elevation {
        val itemShadow = 1.dp
        val navigationBar = 2.dp
    }

    object Button {
        @Composable
        fun disabledContainer() = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)

        @Composable
        fun disabledContent() = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f)
    }

    data class Shapes(
        val corners: RoundedCornerShape = RoundedCornerShape(24.dp),
        val listItem: Dp = 16.dp
    )

    data class ItemsPerRow(
        val artworks: Int = 3,
        val seasons: Int = 3
    )

    data class Global(
        val oldBlurredHeader: Boolean = false,
        val navigationStyle: NavigationStyle = NavigationStyle.PILL
    )

    data class Episodes(
        val large: Boolean = false
    )

    data class Player(
        val waveProgress: Boolean = true
    )

}

val LocalUiShapes = compositionLocalOf { Design.Shapes() }
val LocalUiGlobal = compositionLocalOf { Design.Global() }
val LocalUiItemsPerRow = compositionLocalOf { Design.ItemsPerRow() }
val LocalUiEpisodes = compositionLocalOf { Design.Episodes() }
val LocalUiPlayer = compositionLocalOf { Design.Player() }
val LocalBottomBarPadding = compositionLocalOf { 0.dp }