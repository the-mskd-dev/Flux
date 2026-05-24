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
    data class SetThemeValue(val color: Color) : CustomizationIntent()
}

object CustomizationOptionsDialogs {

    fun theme(currentValue: Color) = FluxOptionsDialogState(
        titleResId = R.string.app_theme,
        currentValue = currentValue,
        options = listOf(
            FluxOptionsDialogItem(value = Color.Red, labelResId = Ui.THEME.LIGHT.stringResourceId),
            FluxOptionsDialogItem(value = Color.Blue, labelResId = Ui.THEME.DARK.stringResourceId),
            FluxOptionsDialogItem(value = Color.Green, labelResId = Ui.THEME.SYSTEM.stringResourceId)
        ),
        applyValue = { value -> CustomizationIntent.SetThemeValue(value) }
    )

}