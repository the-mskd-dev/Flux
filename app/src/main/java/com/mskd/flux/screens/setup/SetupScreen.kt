package com.mskd.flux.screens.setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import flux.shared.generated.resources.welcome_description
import flux.shared.generated.resources.welcome
import flux.shared.generated.resources.welcome_features
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SetupScreen() {

}

@Composable
fun SetupScreenContent() {

    FluxScaffold(
        title = null,
        onBackTap = null,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {}
            ) {
                Text.Label.Large("Next")
            }
        }
    ) { _ ->

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                modifier = Modifier
                    .sizeIn(maxWidth = 600.dp, maxHeight = 600.dp)
                    .fillMaxSize()
                    .alpha(.15f),
                painter = painterResource(Res.drawable.ic_flux),
                contentDescription = "logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
            )

            Column(
                modifier = Modifier
                    .padding(FluxUI.Space.medium)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth(),
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
                        "\uD83C\uDFAC",
                        "\uD83C\uDF7F",
                        "\uD83C\uDFA8"
                    )

                    features.forEachIndexed { index, feature ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                        ) {
                            Text.Body.Large(text = featuresIcons.getOrNull(index))
                            Text.Body.Large(text = feature)
                        }
                    }

                }

            }

        }


    }

}

@Composable
@FluxPreview
fun SetupScreen_Preview() {
    FluxThemePreview {
        SetupScreenContent()
    }
}