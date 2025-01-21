package com.kaem.flux.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
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
        val RoundedCorner get() = RoundedCornerShape(8.dp)
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

