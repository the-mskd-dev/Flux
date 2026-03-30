package com.mskd.flux.screens.settings

import androidx.compose.runtime.Immutable
import com.mskd.flux.R
import com.mskd.flux.ui.theme.Ui

@Immutable
data class SettingsUiState(
    val rewindValue: Int = 10,
    val forwardValue: Int = 10,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val dialogState: SettingsDialogState<*>? = null
)

data class SettingsDialogState<T>(
    val title: Int,
    val currentValue: T,
    val options: Map<T, Pair<String?, Int?>>,
    val applyValue: (T) -> SettingsIntent
) {

    companion object {

        fun rewind(currentValue: Int) = SettingsDialogState(
            title = R.string.button_rewind,
            currentValue = currentValue,
            options = mapOf(
                5 to ("5sec" to null),
                10 to ("10sec" to null),
                15 to ("15sec" to null),
                20 to ("20sec" to null),
                25 to ("25sec" to null),
                30 to ("30sec" to null),
            ),
            applyValue = { value -> SettingsIntent.SetRewindValue(value) }
        )

        fun forward(currentValue: Int) = SettingsDialogState(
            title = R.string.button_forward,
            currentValue = currentValue,
            options = mapOf(
                5 to ("5sec" to null),
                10 to ("10sec" to null),
                15 to ("15sec" to null),
                20 to ("20sec" to null),
                25 to ("25sec" to null),
                30 to ("30sec" to null),
            ),
            applyValue = { value -> SettingsIntent.SetForwardValue(value) }
        )

        fun theme(currentValue: Ui.THEME) = SettingsDialogState(
            title = R.string.app_theme,
            currentValue = currentValue,
            options = mapOf(
                Ui.THEME.LIGHT to (null to Ui.THEME.LIGHT.stringResourceId),
                Ui.THEME.DARK to (null to Ui.THEME.DARK.stringResourceId),
                Ui.THEME.SYSTEM to (null to Ui.THEME.SYSTEM.stringResourceId)
            ),
            applyValue = { value -> SettingsIntent.SetThemeValue(value) }
        )

    }

}

sealed class SettingsIntent {
    object ShowRewindDialog: SettingsIntent()
    data class SetRewindValue(val value: Int): SettingsIntent()
    object ShowForwardDialog: SettingsIntent()
    data class SetForwardValue(val value: Int): SettingsIntent()
    object ShowThemeDialog: SettingsIntent()
    data class SetThemeValue(val theme: Ui.THEME): SettingsIntent()
    object HideDialog : SettingsIntent()
    object OnBackTap: SettingsIntent()
    object OnTokenTap: SettingsIntent()
    object OnHowToTap: SettingsIntent()
    object OnAboutTap: SettingsIntent()
}

sealed class SettingsEvent {
    object BackToPreviousScreen: SettingsEvent()
    object NavigateToTokenScreen: SettingsEvent()
    object NavigateToHowToScreen: SettingsEvent()
    object NavigateToAboutScreen: SettingsEvent()
}