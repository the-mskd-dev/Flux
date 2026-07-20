package com.mskd.flux.features.token.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.features.token.domain.model.TokenMessage


@Immutable
data class TokenUiState(
    val token: String = "",
    val showBackButton: Boolean = false,
    val isLoading: Boolean = false,
    val message: TokenMessage = TokenMessage.None
)

sealed class TokenIntent {
    data class SetToken(val token: String) : TokenIntent()
    data object SaveToken : TokenIntent()
    object OnBackTap: TokenIntent()
    object OnCancelTap: TokenIntent()
    object OnNextTap: TokenIntent()
}

sealed class TokenEvent {
    object BackToPreviousScreen: TokenEvent()
    object NavigateToCatalogScreen: TokenEvent()
}
