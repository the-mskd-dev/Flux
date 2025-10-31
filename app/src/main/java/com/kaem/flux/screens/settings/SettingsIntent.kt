package com.kaem.flux.screens.settings

import com.kaem.flux.ui.theme.Ui
import java.util.Locale

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