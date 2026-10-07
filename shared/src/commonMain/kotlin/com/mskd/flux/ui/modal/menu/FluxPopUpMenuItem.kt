package com.mskd.flux.ui.modal.menu

import androidx.compose.runtime.Composable

data class FluxPopUpMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val leadingIcon:  @Composable (() -> Unit)
)
