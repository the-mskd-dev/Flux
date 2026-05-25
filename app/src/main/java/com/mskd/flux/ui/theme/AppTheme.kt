package com.mskd.flux.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.rememberDynamicColorScheme
import com.mskd.flux.ui.typography.FluxTypography
import com.mskd.flux.utils.extensions.logDescription

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    theme: Ui.THEME = Ui.THEME.SYSTEM,
    color: Int? = null,
    content: @Composable () -> Unit
) {

    val colorScheme = createColorScheme(
        theme = theme,
        color = color
    )

    colorScheme.logDescription()

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = FluxTypography,
        content = content,
    )
}

@Composable
fun createColorScheme(
    theme: Ui.THEME = Ui.THEME.SYSTEM,
    color: Int? = null,
) : ColorScheme {

    val darkTheme: Boolean = when (theme) {
        Ui.THEME.DARK -> true
        Ui.THEME.LIGHT -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (color) {
        null -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) fluxDarkScheme else fluxLightScheme
            }
        }
        else -> {
            rememberDynamicColorScheme(
                seedColor = Color(color),
                isDark = darkTheme
            )
        }
    }

    return colorScheme

}