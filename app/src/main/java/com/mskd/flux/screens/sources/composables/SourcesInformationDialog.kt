package com.mskd.flux.screens.sources.composables

import androidx.compose.runtime.Composable
import com.mskd.flux.features.sources.presentation.SourcesIntent
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text

@Composable
fun SourcesFeatureInformationDialog(
    sendIntent: (SourcesIntent) -> Unit
) {

    FluxDialog(
        onDismiss = { sendIntent(SourcesIntent.CloseDialog) },
        onDismissLabel = "J'ai compris"
    ) {
        Text.Body.Large(
            "Attention nouvelle feature"
        )
    }

}