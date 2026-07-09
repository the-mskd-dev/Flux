package com.mskd.flux.features.customization.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.utils.UiCommon

@Immutable
data class CustomizationUiState(
    val uiTheme: UiCommon.THEME = UiCommon.THEME.SYSTEM,
    val color: Int? = null,
    val waveProgress: Boolean = true,
    val oldBlurredHeader: Boolean = false,
    val largeEpisodeImage: Boolean = false,
    val itemsPerRow: Int = 3,
    val itemsCorners: Int = 12,
    val seasonsPerRow: Int = 3,
    val dialog: CustomizationDialog? = null
)

sealed class CustomizationDialog {
    data class SelectDialog(val state: FluxOptionsDialogState<*, CustomizationIntent>) : CustomizationDialog()
    data class ItemsPerRowDialog(val title: StringProvider, val desc: StringProvider) : CustomizationDialog()
    data class SeasonsPerRowDialog(val title: StringProvider, val desc: StringProvider) : CustomizationDialog()
    data class ItemsCornersDialog(val title: StringProvider, val desc: StringProvider) : CustomizationDialog()
}

sealed class CustomizationIntent {

    // Global
    data object OnBackTap: CustomizationIntent()

    // Dialogs
    data object HideDialog : CustomizationIntent()
    data object ShowColorDialog: CustomizationIntent()
    data object ShowThemeDialog: CustomizationIntent()
    data object ShowItemsPerRowDialog: CustomizationIntent()
    data object ShowSeasonsPerRowDialog: CustomizationIntent()
    data object ShowItemsCornerDialog: CustomizationIntent()

    // Setter
    data class SetColorValue(val color: Int?) : CustomizationIntent()
    data class SetThemeValue(val theme: UiCommon.THEME): CustomizationIntent()
    data class SetItemsPerRowValue(val count: Int): CustomizationIntent()
    data class SetItemsCornersValue(val corners: Int): CustomizationIntent()
    data class SetSeasonsPerRowValue(val count: Int): CustomizationIntent()
    data class OnWaveProgressCheck(val checked: Boolean): CustomizationIntent()
    data class OnOldBlurredHeaderCheck(val checked: Boolean): CustomizationIntent()
    data class OnLargeEpisodeImageCheck(val checked: Boolean): CustomizationIntent()
}

sealed class CustomizationEvent {
    object BackToPreviousScreen: CustomizationEvent()
}