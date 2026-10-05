package com.mskd.flux.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.mskd.flux.ui.colors.fluxDarkScheme
import com.mskd.flux.ui.colors.fluxLightScheme

@Composable
actual fun systemDynamicColorScheme(darkTheme: Boolean): ColorScheme {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) fluxDarkScheme else fluxLightScheme
    }
}