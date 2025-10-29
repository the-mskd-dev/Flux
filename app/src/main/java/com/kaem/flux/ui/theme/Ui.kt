package com.kaem.flux.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaem.flux.R

object Ui {

    object FontSize {
        val SMALL = 12.sp
        val MEDIUM = 17.sp
        val LARGE = 20.sp
        val TITLE = 30.sp
        val BUTTON = 15.sp
    }

    object Space {
        val EXTRA_SMALL = 4.dp
        val SMALL = 8.dp
        val MEDIUM = 16.dp
        val LARGE = 24.dp
    }

    object Weight {
        val LIGHT = FontWeight.W400
        val MEDIUM = FontWeight.W500
        val BOLD = FontWeight.W700
    }

    object Elevation {
        val Level0 = 0.0.dp
        val Level1 = 1.0.dp
        val Level2 = 3.0.dp
        val Level3 = 6.0.dp
        val Level4 = 8.0.dp
        val Level5 = 12.0.dp
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
        }

    }

    object Card {

        @Composable
        fun elevations() = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 4.dp,
            focusedElevation = 12.dp,
            disabledElevation = 0.dp,
            draggedElevation = 12.dp,
            hoveredElevation = 12.dp
        )
    }

    enum class THEME {
        LIGHT, DARK, SYSTEM;

        val stringResourceId: Int get() = when(this) {
            LIGHT -> R.string.light
            DARK -> R.string.dark
            SYSTEM -> R.string.system
        }
    }

}

