package com.mskd.flux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import com.mskd.flux.ui.UiCommon

@Composable
fun createColorScheme(
    theme: UiCommon.THEME = UiCommon.THEME.SYSTEM,
    color: Int? = null,
) : ColorScheme {

    val darkTheme: Boolean = when (theme) {
        UiCommon.THEME.DARK -> true
        UiCommon.THEME.LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (color) {
        null -> systemDynamicColorScheme(darkTheme = darkTheme)
        else -> rememberDynamicColorScheme(seedColor = Color(color), isDark = darkTheme)
    }

    return colorScheme

}