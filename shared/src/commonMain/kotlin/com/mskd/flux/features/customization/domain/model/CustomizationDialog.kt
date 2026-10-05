package com.mskd.flux.features.customization.domain.model

import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.ui.text.TextProvider
import com.mskd.flux.features.customization.presentation.CustomizationIntent

sealed class CustomizationDialog {
    data class SelectDialog(val state: FluxOptionsDialogState<*, CustomizationIntent>) : CustomizationDialog()
    data class ItemsPerRowDialog(val title: TextProvider, val desc: TextProvider) : CustomizationDialog()
    data class SeasonsPerRowDialog(val title: TextProvider, val desc: TextProvider) : CustomizationDialog()
    data class ItemsCornersDialog(val title: TextProvider, val desc: TextProvider) : CustomizationDialog()
}