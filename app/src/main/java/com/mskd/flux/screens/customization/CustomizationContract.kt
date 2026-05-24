package com.mskd.flux.screens.customization

import androidx.compose.runtime.Immutable
import com.mskd.flux.screens.settings.SettingsIntent
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui

@Immutable
data class CustomizationUiState(
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val color: Int? = null,
    val waveProgress: Boolean = true,
    val dialogState: FluxOptionsDialogState<*, CustomizationIntent>? = null,
)

sealed class CustomizationIntent {

    // Global
    data object OnBackTap: CustomizationIntent()

    // Dialogs
    data object HideDialog : CustomizationIntent()
    data object ShowColorDialog: CustomizationIntent()
    data object ShowThemeDialog: CustomizationIntent()

    // Setter
    data class SetColorValue(val color: Int?) : CustomizationIntent()
    data class SetThemeValue(val theme: Ui.THEME): CustomizationIntent()

    data class OnWaveProgressCheck(val checked: Boolean): CustomizationIntent()
}

sealed class CustomizationEvent {
    object BackToPreviousScreen: CustomizationEvent()
}