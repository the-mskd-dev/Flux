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
    data object OnBackTap: CustomizationIntent()
    data object ShowThemeDialog: CustomizationIntent()
    data class SetThemeValue(val color: Color) : CustomizationIntent()
}