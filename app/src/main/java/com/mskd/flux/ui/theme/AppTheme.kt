package com.mskd.flux.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.rememberDynamicColorScheme
import com.mskd.flux.data.repository.connectivity.LocalConnectivity
import com.mskd.flux.ui.typography.FluxTypography
import com.mskd.flux.utils.UiCommon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppTheme(
    theme: UiCommon.THEME = UiCommon.THEME.SYSTEM,
    color: Int? = null,
    isOnline: Boolean = true,
    uiShapes: FluxUI.Shapes = FluxUI.Shapes(),
    uiGlobal: FluxUI.Global = FluxUI.Global(),
    uiItemsPerRow: FluxUI.ItemsPerRow = FluxUI.ItemsPerRow(),
    uiEpisodes: FluxUI.Episodes = FluxUI.Episodes(),
    uiPlayer: FluxUI.Player = FluxUI.Player(),
    content: @Composable () -> Unit
) {

    val colorScheme = createColorScheme(
        theme = theme,
        color = color
    )

    CompositionLocalProvider(
        LocalConnectivity provides isOnline,
        LocalUiShapes provides uiShapes,
        LocalUiGlobal provides uiGlobal,
        LocalUiItemsPerRow provides uiItemsPerRow,
        LocalUiEpisodes provides uiEpisodes,
        LocalUiPlayer provides uiPlayer
    ) {

        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = FluxTypography,
            content = content,
        )
        
    }

}

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