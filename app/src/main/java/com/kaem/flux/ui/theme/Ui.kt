package com.kaem.flux.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.kaem.flux.R

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

    object Card {

        val selectedCardColors @Composable get() = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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

