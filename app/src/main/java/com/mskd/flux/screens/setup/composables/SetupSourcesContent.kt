package com.mskd.flux.screens.setup.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.setup.presentation.SetupContrat
import com.mskd.flux.features.setup.presentation.SetupIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.setup_sources_desc
import flux.shared.generated.resources.setup_sources_title
import flux.shared.generated.resources.welcome
import flux.shared.generated.resources.welcome_description
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupSourcesContent(
    selectedOption: SetupContrat.SourcesOption,
    sendIntent: (SetupIntent) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
    ) {

        Text.Headline.Large(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.setup_sources_title),
            textAlign = TextAlign.Center,
            emphasized = true
        )

        Text.Body.Large(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.setup_sources_desc),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
        ) {

            SetupSourcesItem()
            SetupSourcesItem()

        }

    }

}

@Composable
@Preview
fun SetupSourcesContent_Preview() {
    FluxThemePreview {
        SetupSourcesContent(
            selectedOption = SetupContrat.SourcesOption.DEFAULT,
            sendIntent = {}
        )
    }
}