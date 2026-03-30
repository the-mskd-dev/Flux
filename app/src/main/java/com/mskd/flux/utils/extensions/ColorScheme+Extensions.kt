package com.mskd.flux.utils.extensions

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

fun ColorScheme.logDescription() {

    val colorScheme = this
    fun getRGB(color: Color): String {
        val red = color.red
        val green = color.green
        val blue = color.blue

        val hexRed = (red * 255).toInt()
        val hexGreen = (green * 255).toInt()
        val hexBlue = (blue * 255).toInt()
        val hex = String.format("#%02X%02X%02X", hexRed, hexGreen, hexBlue)

        return "Color(${red}f, ${green}f, ${blue}f), // $hex"
    }

    val color = """
        ColorScheme(
            primary = ${getRGB(colorScheme.primary)}
            onPrimary = ${getRGB(colorScheme.onPrimary)}
            primaryContainer = ${getRGB(colorScheme.primaryContainer)}
            onPrimaryContainer = ${getRGB(colorScheme.onPrimaryContainer)}
            inversePrimary = ${getRGB(colorScheme.inversePrimary)}
            secondary = ${getRGB(colorScheme.secondary)}
            onSecondary = ${getRGB(colorScheme.onSecondary)}
            secondaryContainer = ${getRGB(colorScheme.secondaryContainer)}
            onSecondaryContainer = ${getRGB(colorScheme.onSecondaryContainer)}
            tertiary = ${getRGB(colorScheme.tertiary)}
            onTertiary = ${getRGB(colorScheme.onTertiary)}
            tertiaryContainer = ${getRGB(colorScheme.tertiaryContainer)}
            onTertiaryContainer = ${getRGB(colorScheme.onTertiaryContainer)}
            background = ${getRGB(colorScheme.background)}
            onBackground = ${getRGB(colorScheme.onBackground)}
            surface = ${getRGB(colorScheme.surface)}
            onSurface = ${getRGB(colorScheme.onSurface)}
            surfaceVariant = ${getRGB(colorScheme.surfaceVariant)}
            onSurfaceVariant = ${getRGB(colorScheme.onSurfaceVariant)}
            surfaceTint = ${getRGB(colorScheme.surfaceTint)}
            inverseSurface = ${getRGB(colorScheme.inverseSurface)}
            inverseOnSurface = ${getRGB(colorScheme.inverseOnSurface)}
            error = ${getRGB(colorScheme.error)}
            onError = ${getRGB(colorScheme.onError)}
            errorContainer = ${getRGB(colorScheme.errorContainer)}
            onErrorContainer = ${getRGB(colorScheme.onErrorContainer)}
            outline = ${getRGB(colorScheme.outline)}
            outlineVariant = ${getRGB(colorScheme.outlineVariant)}
            scrim = ${getRGB(colorScheme.scrim)}
            surfaceBright = ${getRGB(colorScheme.surfaceBright)}
            surfaceDim = ${getRGB(colorScheme.surfaceDim)}
            surfaceContainer = ${getRGB(colorScheme.surfaceContainer)}
            surfaceContainerHigh = ${getRGB(colorScheme.surfaceContainerHigh)}
            surfaceContainerHighest = ${getRGB(colorScheme.surfaceContainerHighest)}
            surfaceContainerLow = ${getRGB(colorScheme.surfaceContainerLow)}
            surfaceContainerLowest = ${getRGB(colorScheme.surfaceContainerLowest)}
        )
    """.trimIndent()

    Log.i("ColorScheme", color)

}