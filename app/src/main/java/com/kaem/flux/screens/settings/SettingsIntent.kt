package com.kaem.flux.screens.settings

import com.kaem.flux.ui.theme.Ui
import java.util.Locale

sealed class SettingsIntent {
    data class BackwardDialog(val show: Boolean): SettingsIntent()
    data class SetBackwardValue(val value: Int): SettingsIntent()
    data class ForwardDialog(val show: Boolean): SettingsIntent()
    data class SetForwardValue(val value: Int): SettingsIntent()
    data class ThemeDialog(val show: Boolean): SettingsIntent()
    data class SetThemeValue(val theme: Ui.THEME): SettingsIntent()
    data class SubtitlesDialog(val show: Boolean): SettingsIntent()
    data class SetSubtitlesValue(val locale: Locale): SettingsIntent()
    object OnBackTap: SettingsIntent()
    object OnHowToTap: SettingsIntent()
    object OnAboutTap: SettingsIntent()
}