package com.mskd.flux.model

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource

data class FluxOptionsDialogState<T, out R>(
    val titleResId: StringResource,
    val currentValue: T,
    val options: List<FluxOptionsDialogItem<T>>,
    val applyValue: (T) -> R
)

data class FluxOptionsDialogItem<T>(
    val value: T,
    val label: StringProvider,
    val color: Color? = null
)