package com.mskd.flux.features.settings.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import java.util.Locale

@Immutable
data class SettingsUiState(
    val languageValue: Locale? = null,
    val rewindValue: Int = 10,
    val forwardValue: Int = 10,
    val autoKeyboard: Boolean = false,
    val useExternalPlayer: Boolean = false,
    val pipIsEnabled: Boolean = true,
    val dialogState: FluxOptionsDialogState<*, SettingsIntent>? = null,
    val showSyncDialog: Boolean = false,
    val fullSyncInProgress: Boolean = false,
    val prefetchHdImages: Boolean = false,
    val prefetchImagesState: ImagesPrefetchManager.State = ImagesPrefetchManager.State.Idle
)

sealed class SettingsIntent {

    // Navigation
    data object OnBackTap: SettingsIntent()
    data object OnCustomizationTap: SettingsIntent()
    data object OnTokenTap: SettingsIntent()
    data object OnHowToTap: SettingsIntent()
    data object OnAboutTap: SettingsIntent()
    data object OnSourcesTap: SettingsIntent()

    // Dialogs
    data object HideDialog : SettingsIntent()
    data object ShowLanguageDialog: SettingsIntent()
    data object ShowRewindDialog: SettingsIntent()
    data object ShowForwardDialog: SettingsIntent()
    data class ShowFullSyncDialog(val show: Boolean): SettingsIntent()

    // Setters
    data class SetLanguageValue(val value: Locale?): SettingsIntent()

    data class SetRewindValue(val value: Int): SettingsIntent()
    data class SetForwardValue(val value: Int): SettingsIntent()

    // Others
    data object ProceedFullSync: SettingsIntent()
    data class OnAutoKeyboardCheck(val checked: Boolean): SettingsIntent()
    data class OnExternalPlayerCheck(val checked: Boolean): SettingsIntent()
    data class OnEnablePipCheck(val checked: Boolean): SettingsIntent()
    data class OnPrefetchHdImagesCheck(val checked: Boolean): SettingsIntent()
}

sealed class SettingsEvent {
    data object BackToPreviousScreen: SettingsEvent()
    data object NavigateToCustomizationScreen: SettingsEvent()
    data object NavigateToTokenScreen: SettingsEvent()
    data object NavigateToHowToScreen: SettingsEvent()
    data object NavigateToAboutScreen: SettingsEvent()
    data object NavigateToSourcesScreen: SettingsEvent()
    data object RequestExternalPlayerPermission: SettingsEvent()
}