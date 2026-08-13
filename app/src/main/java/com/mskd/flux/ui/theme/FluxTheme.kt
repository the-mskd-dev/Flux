package com.mskd.flux.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme
import com.mskd.flux.features.connectivity.domain.LocalConnectivity
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.ui.typography.FluxTypography
import com.mskd.flux.utils.UiCommon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluxTheme(
    isOnline: Boolean = true,
    customization: CustomizationDataStore.State = CustomizationDataStore.State(),
    content: @Composable () -> Unit
) {

    val colorScheme = createColorScheme(
        theme = customization.uiTheme,
        color = customization.color
    )

    CompositionLocalProvider(
        LocalConnectivity provides isOnline,
        LocalUiShapes provides FluxUI.Shapes(
            corners = RoundedCornerShape(customization.itemsCorners.dp),
        ),
        LocalUiGlobal provides FluxUI.Global(
            oldBlurredHeader = customization.oldBlurredHeader,
            navigationStyle = customization.navigationStyle
        ),
        LocalUiItemsPerRow provides FluxUI.ItemsPerRow(
            artworks = customization.itemsPerRow,
            seasons = customization.seasonsPerRow
        ),
        LocalUiEpisodes provides FluxUI.Episodes(
            large = customization.largeEpisodeImage
        ),
        LocalUiPlayer provides FluxUI.Player(
            waveProgress = customization.waveProgress
        )
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