package com.mskd.flux.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.mskd.flux.R

object Ui {

    object Space {
        val EXTRA_SMALL = 4.dp
        val SMALL = 8.dp
        val MEDIUM = 16.dp
        val LARGE = 24.dp
    }

    object Shape {

        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        object Corner {

            val ExtraSmall @Composable get() = MaterialTheme.shapes.extraSmall
            val Small @Composable get() = MaterialTheme.shapes.small
            val Medium @Composable get() = MaterialTheme.shapes.medium
            val Large @Composable get() = MaterialTheme.shapes.large
            val LargeIncreased @Composable get() = MaterialTheme.shapes.largeIncreased
            val ExtraLarge @Composable get() = MaterialTheme.shapes.extraLarge
            val ExtraLargeIncreased @Composable get() = MaterialTheme.shapes.extraLargeIncreased
            val ExtraExtraLarge @Composable get() = MaterialTheme.shapes.extraExtraLarge
            val Full @Composable get() = CircleShape

        }

    }

    /**
     * - **Level 0** : base level.
     * - **Level 1** : filled cards and low emphasis items.
     * - **Level 2** : elevated cards and navigation bars.
     * - **Level 3** : cards and floating buttons.
     * - **Level 4** : dialogs and menus.
     * - **Level 5** : modals and navigation drawers.
     */
    object Elevation {
        val Level0 = 0.0.dp
        val Level1 = 1.0.dp
        val Level2 = 3.0.dp
        val Level3 = 6.0.dp
        val Level4 = 8.0.dp
        val Level5 = 12.0.dp
    }

    object Card {

        val selectedCardColors @Composable get() = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

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

    enum class THEME {
        LIGHT, DARK, SYSTEM;

        val stringResourceId: Int get() = when(this) {
            LIGHT -> R.string.light
            DARK -> R.string.dark
            SYSTEM -> R.string.system
        }
    }

    sealed class Colors(val argb: Int?, val stringResId: Int) {
        data object System : Colors(argb = null, stringResId = R.string.system)
        data object Red : Colors(argb = rgbToInt(239, 71, 111), stringResId = R.string.color_red)
        data object Blue : Colors(argb = rgbToInt(17, 138, 178), stringResId = R.string.color_blue)
        data object Green : Colors(argb = rgbToInt(6, 214, 160), stringResId = R.string.color_green)
        data object Yellow : Colors(argb = rgbToInt(255, 209, 102), stringResId = R.string.color_yellow)
        data object Magenta : Colors(argb = rgbToInt(181, 23, 158), stringResId = R.string.color_magenta)
        data object Gray : Colors(argb = rgbToInt(94, 100, 114), stringResId = R.string.color_gray)

        companion object {

            fun rgbToInt(r: Int, g: Int, b: Int, a: Int = 255): Int {
                return (a shl 24) or (r shl 16) or (g shl 8) or b
            }

        }
    }

}

