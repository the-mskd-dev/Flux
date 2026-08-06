package com.mskd.flux.screens.setup.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.setup.presentation.SetupIntent
import com.mskd.flux.screens.sources.composables.sourcesAnnotatedString
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.setup_sources_custom_desc
import flux.shared.generated.resources.setup_sources_custom_title
import flux.shared.generated.resources.setup_sources_default_desc
import flux.shared.generated.resources.setup_sources_default_title
import flux.shared.generated.resources.setup_sources_desc
import flux.shared.generated.resources.setup_sources_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupSourcesContent(
    systemFoldersEnabled: Boolean,
    sendIntent: (SetupIntent) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
        ) {

            Text.MainTitle(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.setup_sources_title),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text.Content.Body(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.setup_sources_desc),
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.listItem)
            ) {

                SetupSourcesItem(
                    title = stringResource(Res.string.setup_sources_default_title),
                    description = sourcesAnnotatedString(Res.string.setup_sources_default_desc),
                    isSelected = systemFoldersEnabled,
                    onClick = { sendIntent(SetupIntent.EnableSystemFolders(enabled = true)) }
                )

                SetupSourcesItem(
                    title = stringResource(Res.string.setup_sources_custom_title),
                    description = AnnotatedString(stringResource(Res.string.setup_sources_custom_desc)),
                    isSelected = !systemFoldersEnabled,
                    onClick = { sendIntent(SetupIntent.EnableSystemFolders(enabled = false)) }
                )

            }

        }

    }

}

@Composable
@Preview
fun SetupSourcesContent_Preview() {
    FluxThemePreview {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FluxUI.Space.medium)
        ) {
            SetupSourcesContent(
                systemFoldersEnabled = true,
                sendIntent = {}
            )
        }
    }
}