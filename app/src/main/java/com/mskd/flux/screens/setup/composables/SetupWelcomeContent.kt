package com.mskd.flux.screens.setup.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
                text = stringResource(Res.string.welcome),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text.Content.Body(
                text = stringResource(Res.string.welcome_description),
                color = MaterialTheme.colorScheme.onSurface
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
                        Text.Content.Body(
                            text = featuresIcons.getOrNull(index) ?: "✨",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text.Content.Body(
                            text = feature,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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