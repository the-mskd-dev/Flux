package com.mskd.flux.screens.customization

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.mskd.flux.R
import com.mskd.flux.screens.settings.SettingsIntent
import com.mskd.flux.ui.component.FluxOptionsDialogItem
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui
import java.util.Locale

@Immutable
data class CustomizationUiState(
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
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
    data class SetColorValue(val color: Color) : CustomizationIntent()
    data class SetThemeValue(val theme: Ui.THEME): CustomizationIntent()
}

sealed class CustomizationEvent {
    object BackToPreviousScreen: CustomizationEvent()
}