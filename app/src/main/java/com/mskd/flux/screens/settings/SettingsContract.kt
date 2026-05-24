package com.mskd.flux.screens.settings

import androidx.compose.runtime.Immutable
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