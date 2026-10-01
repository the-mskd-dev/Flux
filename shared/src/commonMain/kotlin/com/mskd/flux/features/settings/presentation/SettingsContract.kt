package com.mskd.flux.features.settings.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.settings.domain.model.SettingsDialog
import java.util.Locale

@Immutable
data class SettingsUiState(
    val languageValue: Locale? = null,
    val rewindValue: Int = 10,
    val forwardValue: Int = 10,
    val autoKeyboard: Boolean = false,
    val useExternalPlayer: Boolean = false,
    val pipIsEnabled: Boolean = true,
    val optionsDialog: FluxOptionsDialogState<*, SettingsIntent>? = null,
    val settingsDialog: SettingsDialog? = null,
    val fullSyncInProgress: Boolean = false,
    val prefetchHdImages: Boolean = false,
    val prefetchImagesState: ImagesPrefetchManager.State = ImagesPrefetchManager.State.Idle,
    val privateFolderEnabled: Boolean = false,
    val privateFolderIncludeNsfw: Boolean = true,
    val privateFolderPinDialog: PrivateFolderPinDialog? = null,
    val privateFolderPinError: Boolean = false,
    val appVersion: String? = null
)

enum class PrivateFolderPinDialog {
    CREATE, VERIFY_TO_DISABLE;

    companion object {
        const val PIN_LENGTH = 4
    }
}

sealed interface SettingsIntent {

    // Navigation
    data object OnBackTap: SettingsIntent
    data object OnCustomizationClick: SettingsIntent
    data object OnTokenTap: SettingsIntent
    data object OnHowToTap: SettingsIntent
    data object OnAboutTap: SettingsIntent
    data object OnSourcesTap: SettingsIntent

    // Dialogs
    data object HideDialog : SettingsIntent
    data object ShowLanguageDialog: SettingsIntent
    data object ShowRewindDialog: SettingsIntent
    data object ShowForwardDialog: SettingsIntent
    data class ShowSettingsDialog(val dialog: SettingsDialog?): SettingsIntent

    // Private folder
    data class OnPrivateFolderCheck(val checked: Boolean): SettingsIntent
    data class OnPrivateFolderIncludeNsfwCheck(val checked: Boolean): SettingsIntent
    data object ClearPrivateFolderPinError: SettingsIntent
    data class SubmitPrivateFolderPin(val pin: String): SettingsIntent
    data object HidePrivateFolderPinDialog: SettingsIntent

    // Setters
    data class SetLanguageValue(val value: Locale?): SettingsIntent

    data class SetRewindValue(val value: Int): SettingsIntent
    data class SetForwardValue(val value: Int): SettingsIntent

    // Others
    data object ProceedFullSync: SettingsIntent
    data class OnAutoKeyboardCheck(val checked: Boolean): SettingsIntent
    data class OnExternalPlayerCheck(val checked: Boolean): SettingsIntent
    data class OnEnablePipCheck(val checked: Boolean): SettingsIntent
    data class OnPrefetchHdImagesCheck(val checked: Boolean): SettingsIntent
    data class OpenUrl(val url: String): SettingsIntent
    data object SendEmail: SettingsIntent
}

sealed interface SettingsEvent {
    data object BackToPreviousScreen: SettingsEvent
    data object NavigateToCustomizationScreen: SettingsEvent
    data object NavigateToTokenScreen: SettingsEvent
    data object NavigateToHowToScreen: SettingsEvent
    data object NavigateToAboutScreen: SettingsEvent
    data object NavigateToSourcesScreen: SettingsEvent
    data object RequestExternalPlayerPermission: SettingsEvent
    data object PrivateFolderPinUpdated: SettingsEvent
}