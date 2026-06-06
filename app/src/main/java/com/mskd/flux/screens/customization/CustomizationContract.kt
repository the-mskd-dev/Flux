package com.mskd.flux.screens.customization

import androidx.compose.runtime.Immutable
import com.mskd.flux.ui.component.global.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui

@Immutable
data class CustomizationUiState(
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val color: Int? = null,
    val waveProgress: Boolean = true,
    val largeEpisodeImage: Boolean = false,
    val itemsPerRow: Int = 3,
    val dialog: CustomizationDialog? = null
)

sealed class CustomizationDialog {
    data class SelectDialog(val state: FluxOptionsDialogState<*, CustomizationIntent>) : CustomizationDialog()
    data object ItemsPerRowDialog : CustomizationDialog()
}

sealed class CustomizationIntent {

    // Global
    data object OnBackTap: CustomizationIntent()

    // Dialogs
    data object HideDialog : CustomizationIntent()
    data object ShowColorDialog: CustomizationIntent()
    data object ShowThemeDialog: CustomizationIntent()
    data object ShowItemsPerRowDialog: CustomizationIntent()

    // Setter
    data class SetColorValue(val color: Int?) : CustomizationIntent()
    data class SetThemeValue(val theme: Ui.THEME): CustomizationIntent()
    data class SetItemsPerRowValue(val count: Int): CustomizationIntent()
    data class OnWaveProgressCheck(val checked: Boolean): CustomizationIntent()
    data class OnLargeEpisodeImageCheck(val checked: Boolean): CustomizationIntent()
}

sealed class CustomizationEvent {
    object BackToPreviousScreen: CustomizationEvent()
}