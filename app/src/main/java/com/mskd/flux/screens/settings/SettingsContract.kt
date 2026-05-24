package com.mskd.flux.screens.settings

import androidx.compose.runtime.Immutable
import com.mskd.flux.R
import com.mskd.flux.ui.component.FluxOptionsDialogItem
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.useCases.images.ImagesUC
import java.util.Locale

@Immutable
data class SettingsUiState(
    val languageValue: Locale? = null,
    val rewindValue: Int = 10,
    val forwardValue: Int = 10,
    val uiTheme: Ui.THEME = Ui.THEME.SYSTEM,
    val autoKeyboard: Boolean = false,
    val useExternalPlayer: Boolean = false,
    val dialogState: FluxOptionsDialogState<*, SettingsIntent>? = null,
    val showSyncDialog: Boolean = false,
    val fullSyncInProgress: Boolean = false,
    val prefetchImages: Boolean = false,
    val prefetchImagesState: ImagesUC.State = ImagesUC.State.Idle
)

object SettingsOptionsDialogs {

    fun language(currentValue: Locale?) = FluxOptionsDialogState(
        titleResId = R.string.information_language,
        currentValue = currentValue,
        options = listOf(
            FluxOptionsDialogItem(value = null, labelResId = R.string.system),
            FluxOptionsDialogItem(value = Locale.ENGLISH, label = Locale.ENGLISH.displayLanguage),
            FluxOptionsDialogItem(value = Locale.FRENCH, label = Locale.FRENCH.displayLanguage),
            FluxOptionsDialogItem(value = Locale.GERMAN , label = Locale.GERMAN.displayLanguage),
            FluxOptionsDialogItem(value = Locale.ITALIAN, label = Locale.ITALIAN.displayLanguage),
            FluxOptionsDialogItem(value = Locale.JAPANESE, label = Locale.JAPANESE.displayLanguage),
            FluxOptionsDialogItem(value = Locale.KOREAN, label = Locale.KOREAN.displayLanguage),
            Locale.forLanguageTag("es").let { FluxOptionsDialogItem(value = it, label = it.displayLanguage) }
        ),
        applyValue = { value -> SettingsIntent.SetLanguageValue(value) }
    )

    fun rewind(currentValue: Int) = FluxOptionsDialogState(
        titleResId = R.string.button_rewind,
        currentValue = currentValue,
        options = listOf(
            FluxOptionsDialogItem(value = 5, label = "5sec"),
            FluxOptionsDialogItem(value = 10, label = "10sec"),
            FluxOptionsDialogItem(value = 30, label = "30sec")
        ),
        applyValue = { value -> SettingsIntent.SetRewindValue(value) }
    )

    fun forward(currentValue: Int) = FluxOptionsDialogState(
        titleResId = R.string.button_forward,
        currentValue = currentValue,
        options = listOf(
            FluxOptionsDialogItem(value = 5, label = "5sec"),
            FluxOptionsDialogItem(value = 10, label = "10sec"),
            FluxOptionsDialogItem(value = 30, label = "30sec")
        ),
        applyValue = { value -> SettingsIntent.SetForwardValue(value) }
    )

    fun theme(currentValue: Ui.THEME) = FluxOptionsDialogState(
        titleResId = R.string.app_theme,
        currentValue = currentValue,
        options = listOf(
            FluxOptionsDialogItem(value = Ui.THEME.LIGHT, labelResId = Ui.THEME.LIGHT.stringResourceId),
            FluxOptionsDialogItem(value = Ui.THEME.DARK, labelResId = Ui.THEME.DARK.stringResourceId),
            FluxOptionsDialogItem(value = Ui.THEME.SYSTEM, labelResId = Ui.THEME.SYSTEM.stringResourceId)
        ),
        applyValue = { value -> SettingsIntent.SetThemeValue(value) }
    )

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
    data class OnPrefetchImagesCheck(val checked: Boolean): SettingsIntent()
}

sealed class SettingsEvent {
    object BackToPreviousScreen: SettingsEvent()
    object NavigateToTokenScreen: SettingsEvent()
    object NavigateToHowToScreen: SettingsEvent()
    object NavigateToAboutScreen: SettingsEvent()
    object RequestExternalPlayerPermission: SettingsEvent()
}