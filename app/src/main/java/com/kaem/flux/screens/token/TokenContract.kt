package com.kaem.flux.screens.token

import androidx.compose.runtime.Immutable
import com.kaem.flux.screens.settings.SettingsEvent
import com.kaem.flux.screens.settings.SettingsIntent


@Immutable
data class TokenUiState(
    val token: String = "",
    val showBackButton: Boolean = false
)

sealed class TokenIntent {
    data class SetToken(val token: String) : TokenIntent()
    data object SaveToken : TokenIntent()
    data object TapOnTMDB : TokenIntent()
    data object TapOnGetToken : TokenIntent()
    object OnBackTap: TokenIntent()
}

sealed class TokenEvent {
    data object NavigateToTMDB : TokenEvent()
    data object NavigateToGetToken : TokenEvent()
    object BackToPreviousScreen: TokenEvent()
}
