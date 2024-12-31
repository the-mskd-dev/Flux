package com.kaem.flux.ui.theme

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FluxFontSize {
    val SMALL = 14.sp
    val MEDIUM = 17.sp
    val LARGE = 20.sp
    val TITLE = 30.sp
}

object FluxSpace {
    val EXTRA_SMALL = 4.dp
    val SMALL = 8.dp
    val MEDIUM = 16.dp
    val LARGE = 24.dp
}

object FluxWeight {
    val LIGHT = FontWeight.W400
    val MEDIUM = FontWeight.W500
    val BOLD = FontWeight.W700
}

object FluxElevation {
    val Level0 = 0.0.dp
    val Level1 = 1.0.dp
    val Level2 = 3.0.dp
    val Level3 = 6.0.dp
    val Level4 = 8.0.dp
    val Level5 = 12.0.dp

    @Composable
    fun buttonElevation(
        defaultElevation: Dp = Level1,
        pressedElevation: Dp = Level1,
        focusedElevation: Dp = Level1,
        hoveredElevation: Dp = Level2,
        disabledElevation: Dp = Level1
    ): ButtonElevation = ButtonDefaults.elevatedButtonElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        disabledElevation = disabledElevation
    )

    @Composable
    fun floatingButtonElevation(
        defaultElevation: Dp = Level1,
        pressedElevation: Dp = Level1,
        focusedElevation: Dp = Level1,
        hoveredElevation: Dp = Level2,
    ): FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
    )

}
