package com.mskd.flux.screens.customization

import androidx.compose.runtime.Immutable
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui

@Immutable
data class CustomizationUiState(
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val dialogState: FluxOptionsDialogState<*, CustomizationIntent>? = null,
)

sealed class CustomizationIntent