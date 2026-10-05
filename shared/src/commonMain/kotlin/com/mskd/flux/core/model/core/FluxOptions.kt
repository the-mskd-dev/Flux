package com.mskd.flux.core.model.core

import androidx.compose.ui.graphics.Color
import com.mskd.flux.ui.text.TextProvider
import org.jetbrains.compose.resources.StringResource

data class FluxOptionsDialogState<T, out R>(
    val titleResId: StringResource,
    val currentValue: T,
    val options: List<FluxOptionsDialogItem<T>>,
    val applyValue: (T) -> R
)

data class FluxOptionsDialogItem<T>(
    val value: T,
    val label: TextProvider,
    val color: Color? = null
)