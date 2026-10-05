package com.mskd.flux.ui.global

import androidx.compose.runtime.Composable

@Composable
expect fun BackGesture(
    enabled: Boolean = true,
    onBack: () -> Unit
)