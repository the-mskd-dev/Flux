package com.mskd.flux.screens.sources.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.sources.presentation.SourcesIntent
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.dialog_sources_description
import flux.shared.generated.resources.dialog_sources_title
import flux.shared.generated.resources.got_it
import org.jetbrains.compose.resources.stringResource

@Composable
fun SourcesInformationDialog(
    sendIntent: (SourcesIntent) -> Unit
) {

    FluxDialog(
        title = stringResource(Res.string.dialog_sources_title),
        onDismiss = { sendIntent(SourcesIntent.CloseDialog) },
        onDismissLabel = stringResource(Res.string.got_it)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Text.Annotated(
                text = sourcesAnnotatedString(Res.string.dialog_sources_description),
                style = MaterialTheme.typography.bodyLarge
            )

        }

    }

}

@Preview
@Composable
fun SourcesInformationDialog_Preview() {
    FluxTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            SourcesInformationDialog {}
        }
    }
}