package com.mskd.flux.features.message.presentation

sealed interface MessageIntent {
    data object OnActionClick: MessageIntent
}