package com.kaem.flux.screens.settings

import androidx.compose.runtime.Immutable
import com.kaem.flux.R
import com.kaem.flux.ui.theme.Ui
import java.util.Locale

@Immutable
data class SettingsUiState(
    val backwardValue: Int = 10,
    val forwardValue: Int = 10,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val subtitlesLanguage: Locale = Locale.getDefault(),
    val dialogState: SettingsDialogState<*>? = null
)

data class SettingsDialogState<T>(
    val title: Int,
    val currentValue: T,
    val options: Map<T, Pair<String?, Int?>>,
    val applyValue: (T) -> SettingsIntent
) {

    companion object {

        fun backward(currentValue: Int) = SettingsDialogState(
            title = R.string.button_backward,
            currentValue = currentValue,
            options = mapOf(
                5 to ("5sec" to null),
                10 to ("10sec" to null),
                15 to ("15sec" to null),
                20 to ("20sec" to null),
                25 to ("25sec" to null),
                30 to ("30sec" to null),
            ),
            applyValue = { value -> SettingsIntent.SetBackwardValue(value) }
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
                //Ui.THEME.LIGHT to (null to Ui.THEME.LIGHT.stringResourceId),
                Ui.THEME.DARK to (null to Ui.THEME.DARK.stringResourceId),
                Ui.THEME.SYSTEM to (null to Ui.THEME.SYSTEM.stringResourceId)
            ),
            applyValue = { value -> SettingsIntent.SetThemeValue(value) }
        )

        fun subtitles(currentValue: Locale) = SettingsDialogState(
            title = R.string.subtitles_language,
            currentValue = currentValue,
            options = mapOf(
                Locale.ENGLISH to (Locale.ENGLISH.displayLanguage to null),
                Locale.FRENCH to (Locale.FRENCH.displayLanguage to null),
                Locale.ITALIAN to (Locale.ITALIAN.displayLanguage to null),
                Locale.JAPANESE to (Locale.JAPANESE.displayLanguage to null),
                Locale.CHINESE to (Locale.CHINESE.displayLanguage to null),
                Locale.KOREAN to (Locale.KOREAN.displayLanguage to null),
            ),
            applyValue = { value -> SettingsIntent.SetSubtitlesValue(value) }
        )

    }

}

sealed class SettingsIntent {
    object ShowBackwardDialog: SettingsIntent()
    data class SetBackwardValue(val value: Int): SettingsIntent()
    object ShowForwardDialog: SettingsIntent()
    data class SetForwardValue(val value: Int): SettingsIntent()
    object ShowThemeDialog: SettingsIntent()
    data class SetThemeValue(val theme: Ui.THEME): SettingsIntent()
    object ShowSubtitlesDialog: SettingsIntent()
    data class SetSubtitlesValue(val locale: Locale): SettingsIntent()
    object HideDialog : SettingsIntent()
    object OnBackTap: SettingsIntent()
    object OnHowToTap: SettingsIntent()
    object OnAboutTap: SettingsIntent()
}

sealed class SettingsEvent {
    object BackToPreviousScreen: SettingsEvent()
    object NavigateToHowToScreen: SettingsEvent()
    object NavigateToAboutScreen: SettingsEvent()
}