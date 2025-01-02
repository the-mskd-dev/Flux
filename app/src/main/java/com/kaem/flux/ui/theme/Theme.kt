package com.kaem.flux.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FluxColorScheme = darkColorScheme(
    primary = Coral, // Most important, like buttons, titles, navigation bars...
    onPrimary = Color.White,
    primaryContainer = Coral,
    onPrimaryContainer = Color.White,
    secondary = LightCoral, // Less important, background, borders, texts
    tertiary = ContrastCoral, // Additional color for contrast,
    background = Color.Black,
    surface = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White,
)

private val FluxLightColorScheme = lightColorScheme(
    primary = Coral, // Most important, like buttons, titles, navigation bars...
    onPrimary = Color.White,
    primaryContainer = Coral,
    onPrimaryContainer = Color.White,
    secondary = LightCoral, // Less important, background, borders, texts
    tertiary = ContrastCoral, // Additional color for contrast,
    background = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    onBackground = Color.Black,
)

@Composable
fun FluxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val dark = true // darkTheme
    val colorScheme = when {

        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        dark -> FluxColorScheme

        else -> FluxLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {

        val transparent = Color(0x00000000)

        SideEffect {

            val window = (view.context as Activity).window
            window.statusBarColor = transparent.toArgb()
            window.navigationBarColor = transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false//darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}