package com.mskd.flux.screens.setup.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.welcome
import flux.shared.generated.resources.welcome_description
import flux.shared.generated.resources.welcome_features
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupWelcomeContent() {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
    ) {

        Text.Headline.Large(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.welcome),
            textAlign = TextAlign.Center,
            emphasized = true
        )

        Text.Body.Large(
            text = stringResource(Res.string.welcome_description),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            val features = stringArrayResource(Res.array.welcome_features)
            val featuresIcons = listOf(
                "\uD83C\uDF7F",
                "\uD83D\uDCFA",
                "\uD83C\uDFA8",
            )

            features.forEachIndexed { index, feature ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                ) {
                    Text.Body.Large(text = featuresIcons.getOrNull(index) ?: "✨")
                    Text.Body.Large(text = feature)
                }
            }

        }

    }

}

@Composable
@FluxPreview
fun SetupWelcomeContent_Preview() {
    FluxThemePreview {
        SetupWelcomeContent()
    }
}