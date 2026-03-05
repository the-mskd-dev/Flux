package com.kaem.flux.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.kaem.flux.ui.typography.FluxTypography
import com.kaem.flux.utils.extensions.logDescription

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    theme: Ui.THEME = Ui.THEME.SYSTEM,
    content: @Composable () -> Unit
) {

    val darkTheme: Boolean = isSystemInDarkTheme()

    val colorScheme = when (theme) {
        Ui.THEME.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) fluxDarkScheme else fluxLightScheme
            }

        }

        Ui.THEME.LIGHT -> fluxLightScheme
        else -> fluxDarkScheme
    }
    /*val view = LocalView.current
    if (!view.isInEditMode) {

        val transparent = Color(0x00000000)

        SideEffect {

            val window = (view.context as Activity).window
            window.statusBarColor = transparent.toArgb()
            window.navigationBarColor = transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false//darkTheme
        }
    }*/

    colorScheme.logDescription()

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = FluxTypography,
        content = content,
    )
}