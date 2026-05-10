package com.mskd.flux.screens.settings

import androidx.compose.runtime.Immutable
import com.mskd.flux.R
import com.mskd.flux.ui.theme.Ui
import java.util.Locale

@Immutable
data class SettingsUiState(
    val languageValue: Locale? = null,
    val rewindValue: Int = 10,
    val forwardValue: Int = 10,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val autoKeyboard: Boolean = false,
    val useExternalPlayer: Boolean = false,
    val dialogState: SettingsDialogState<*>? = null,
    val showSyncDialog: Boolean = false
)

data class SettingsDialogState<T>(
    val title: Int,
    val currentValue: T,
    val options: Map<T, Pair<String?, Int?>>,
    val applyValue: (T) -> SettingsIntent
) {

    companion object {

        fun language(currentValue: Locale?) = SettingsDialogState(
            title = R.string.information_language,
            currentValue = currentValue,
            options = mapOf(
                null to (null to R.string.system),
                Locale.ENGLISH to (Locale.ENGLISH.displayLanguage to null),
                Locale.FRENCH to (Locale.FRENCH.displayLanguage to null),
                Locale.GERMAN to (Locale.GERMAN.displayLanguage to null),
                Locale.ITALIAN to (Locale.ITALIAN.displayLanguage to null),
                Locale.JAPANESE to (Locale.JAPANESE.displayLanguage to null),
                Locale.KOREAN to (Locale.KOREAN.displayLanguage to null),
                Locale.forLanguageTag("es").let { it to (it.displayLanguage to null) },
            ),
            applyValue = { value -> SettingsIntent.SetLanguageValue(value) }
        )

        fun rewind(currentValue: Int) = SettingsDialogState(
            title = R.string.button_rewind,
            currentValue = currentValue,
            options = mapOf(
                5 to ("5sec" to null),
                10 to ("10sec" to null),
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

    data object ShowLanguageDialog: SettingsIntent()
    data class SetLanguageValue(val value: Locale?): SettingsIntent()

    data object ShowRewindDialog: SettingsIntent()
    data class SetRewindValue(val value: Int): SettingsIntent()
    data object ShowForwardDialog: SettingsIntent()
    data class SetForwardValue(val value: Int): SettingsIntent()
    data object ShowThemeDialog: SettingsIntent()
    data class SetThemeValue(val theme: Ui.THEME): SettingsIntent()
    data object HideDialog : SettingsIntent()
    data object OnBackTap: SettingsIntent()
    data object OnTokenTap: SettingsIntent()
    data object OnHowToTap: SettingsIntent()
    data object OnAboutTap: SettingsIntent()

    data class ShowFullSyncDialog(val show: Boolean): SettingsIntent()
    data object ProceedFullSync: SettingsIntent()
    data class OnAutoKeyboardCheck(val checked: Boolean): SettingsIntent()
    data class OnExternalPlayerCheck(val checked: Boolean): SettingsIntent()
}

sealed class SettingsEvent {
    object BackToPreviousScreen: SettingsEvent()
    object NavigateToTokenScreen: SettingsEvent()
    object NavigateToHowToScreen: SettingsEvent()
    object NavigateToAboutScreen: SettingsEvent()
    object RequestExternalPlayerPermission: SettingsEvent()
}