package com.kaem.flux.screens.token

import androidx.compose.runtime.Immutable


@Immutable
data class TokenUiState(
    val token: String = "",
    val showBackButton: Boolean = false,
    val showNextButton: Boolean = false,
    val isLoading: Boolean = false,
    val message: TokenMessage = TokenMessage.None
)

sealed class TokenMessage {
    data object Success : TokenMessage()
    data object Error : TokenMessage()
    data object None : TokenMessage()
}

sealed class TokenIntent {
    data class SetToken(val token: String) : TokenIntent()
    data object SaveToken : TokenIntent()
    object OnBackTap: TokenIntent()
    object OnNextTap: TokenIntent()
}

sealed class TokenEvent {
    object BackToPreviousScreen: TokenEvent()
    object NavigateToHomeScreen: TokenEvent()
}
