package com.mskd.flux.features.token.domain.model

sealed class TokenMessage {
    data object Success : TokenMessage()
    data object Error : TokenMessage()
    data object None : TokenMessage()
}