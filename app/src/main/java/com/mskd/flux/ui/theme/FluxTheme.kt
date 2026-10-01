package com.mskd.flux.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme
import com.mskd.flux.features.connectivity.domain.LocalConnectivity
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.LocalUiEpisodes
import com.mskd.flux.presentation.LocalUiGlobal
import com.mskd.flux.presentation.LocalUiItemsPerRow
import com.mskd.flux.presentation.LocalUiPlayer
import com.mskd.flux.presentation.LocalUiShapes
import com.mskd.flux.presentation.blurBackground
import com.mskd.flux.presentation.modal.LocalModalHost
import com.mskd.flux.presentation.modal.ModalHostState
import com.mskd.flux.presentation.modal.bottomSheet.FluxBottomSheetHost
import com.mskd.flux.presentation.modal.dialog.FluxDialogHost
import com.mskd.flux.presentation.text.LocalEmphasizedTypography
import com.mskd.flux.presentation.text.fluxEmphasizedTypography
import com.mskd.flux.presentation.text.fluxTypography
import com.mskd.flux.utils.UiCommon
import dev.chrisbanes.haze.rememberHazeState

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

    val modalHost = remember { ModalHostState() }
    val modalHazeState = rememberHazeState()

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
        ),
        LocalEmphasizedTypography provides fluxEmphasizedTypography(),
        LocalModalHost provides modalHost
    ) {

        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = fluxTypography(),
        ) {

            Box(Modifier.fillMaxSize()) {

                Box(Modifier.fillMaxSize().blurBackground(modalHazeState)) {

                    content()

                }

                FluxBottomSheetHost(
                    host = modalHost,
                    hazeState = modalHazeState
                )

                FluxDialogHost(
                    host = modalHost,
                    hazeState = modalHazeState
                )
            }

        }
        
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