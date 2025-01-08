package com.kaem.flux.utils.extensions

import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

fun ColorScheme.log() {

    val colorScheme = this
    fun getRGB(color: Color): Triple<Float, Float, Float> {
        val red = color.red
        val green = color.green
        val blue = color.blue
        return Triple(red, green, blue)
    }

    Log.i("ColorScheme", "primary : ${getRGB(colorScheme.primary)}")
    Log.i("ColorScheme", "onPrimary : ${getRGB(colorScheme.onPrimary)}")
    Log.i("ColorScheme", "primaryContainer : ${getRGB(colorScheme.primaryContainer)}")
    Log.i("ColorScheme", "onPrimaryContainer : ${getRGB(colorScheme.onPrimaryContainer)}")
    Log.i("ColorScheme", "inversePrimary : ${getRGB(colorScheme.inversePrimary)}")
    Log.i("ColorScheme", "secondary : ${getRGB(colorScheme.secondary)}")
    Log.i("ColorScheme", "onSecondary : ${getRGB(colorScheme.onSecondary)}")
    Log.i("ColorScheme", "secondaryContainer : ${getRGB(colorScheme.secondaryContainer)}")
    Log.i("ColorScheme", "onSecondaryContainer : ${getRGB(colorScheme.onSecondaryContainer)}")
    Log.i("ColorScheme", "tertiary : ${getRGB(colorScheme.tertiary)}")
    Log.i("ColorScheme", "onTertiary : ${getRGB(colorScheme.onTertiary)}")
    Log.i("ColorScheme", "tertiaryContainer : ${getRGB(colorScheme.tertiaryContainer)}")
    Log.i("ColorScheme", "onTertiaryContainer : ${getRGB(colorScheme.onTertiaryContainer)}")
    Log.i("ColorScheme", "background : ${getRGB(colorScheme.background)}")
    Log.i("ColorScheme", "onBackground : ${getRGB(colorScheme.onBackground)}")
    Log.i("ColorScheme", "surface : ${getRGB(colorScheme.surface)}")
    Log.i("ColorScheme", "onSurface : ${getRGB(colorScheme.onSurface)}")
    Log.i("ColorScheme", "surfaceVariant : ${getRGB(colorScheme.surfaceVariant)}")
    Log.i("ColorScheme", "onSurfaceVariant : ${getRGB(colorScheme.onSurfaceVariant)}")
    Log.i("ColorScheme", "surfaceTint : ${getRGB(colorScheme.surfaceTint)}")
    Log.i("ColorScheme", "inverseSurface : ${getRGB(colorScheme.inverseSurface)}")
    Log.i("ColorScheme", "inverseOnSurface : ${getRGB(colorScheme.inverseOnSurface)}")
    Log.i("ColorScheme", "error : ${getRGB(colorScheme.error)}")
    Log.i("ColorScheme", "onError : ${getRGB(colorScheme.onError)}")
    Log.i("ColorScheme", "errorContainer : ${getRGB(colorScheme.errorContainer)}")
    Log.i("ColorScheme", "onErrorContainer : ${getRGB(colorScheme.onErrorContainer)}")
    Log.i("ColorScheme", "outline : ${getRGB(colorScheme.outline)}")
    Log.i("ColorScheme", "outlineVariant : ${getRGB(colorScheme.outlineVariant)}")
    Log.i("ColorScheme", "scrim : ${getRGB(colorScheme.scrim)}")
    Log.i("ColorScheme", "surfaceBright : ${getRGB(colorScheme.surfaceBright)}")
    Log.i("ColorScheme", "surfaceDim : ${getRGB(colorScheme.surfaceDim)}")
    Log.i("ColorScheme", "surfaceContainer : ${getRGB(colorScheme.surfaceContainer)}")
    Log.i("ColorScheme", "surfaceContainerHigh : ${getRGB(colorScheme.surfaceContainerHigh)}")
    Log.i("ColorScheme", "surfaceContainerHighest : ${getRGB(colorScheme.surfaceContainerHighest)}")
    Log.i("ColorScheme", "surfaceContainerLow : ${getRGB(colorScheme.surfaceContainerLow)}")
    Log.i("ColorScheme", "surfaceContainerLowest : ${getRGB(colorScheme.surfaceContainerLowest)}")
}