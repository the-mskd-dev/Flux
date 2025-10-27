package com.kaem.flux.screens.settings

sealed class SettingsEvent {
    object BackToPreviousScreen: SettingsEvent()
    object NavigateToHowToScreen: SettingsEvent()
    object NavigateToAboutScreen: SettingsEvent()
}