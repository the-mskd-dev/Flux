package com.mskd.flux.ui.global

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackGesture(
    enabled: Boolean,
    onBack: () -> Unit
) {
    BackHandler(
        enabled = enabled,
        onBack = onBack
    )
}