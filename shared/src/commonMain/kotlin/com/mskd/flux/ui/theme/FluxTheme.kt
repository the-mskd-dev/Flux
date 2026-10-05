package com.mskd.flux.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.connectivity.domain.LocalConnectivity
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.ui.FluxUI
import com.mskd.flux.ui.LocalUiEpisodes
import com.mskd.flux.ui.LocalUiGlobal
import com.mskd.flux.ui.LocalUiItemsPerRow
import com.mskd.flux.ui.LocalUiPlayer
import com.mskd.flux.ui.LocalUiShapes
import com.mskd.flux.ui.blurBackground
import com.mskd.flux.ui.modal.LocalModalHost
import com.mskd.flux.ui.modal.ModalHostState
import com.mskd.flux.ui.modal.bottomSheet.FluxBottomSheetHost
import com.mskd.flux.ui.modal.dialog.FluxDialogHost
import com.mskd.flux.ui.text.LocalEmphasizedTypography
import com.mskd.flux.ui.text.fluxEmphasizedTypography
import com.mskd.flux.ui.text.fluxTypography
import dev.chrisbanes.haze.rememberHazeState

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