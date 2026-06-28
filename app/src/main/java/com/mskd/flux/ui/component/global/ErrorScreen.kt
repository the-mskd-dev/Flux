package com.mskd.flux.ui.component.global

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.oups_an_error_occured
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    message: String,
    description: String? = null,
    onBackButtonTap: () -> Unit
) {

    FluxScaffold(
        onBackTap = onBackButtonTap,
        title = null
    ) {

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text.Title.Large(
                    modifier = Modifier.fillMaxWidth(),
                    text = message,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text.Body.Medium(
                    modifier = Modifier.fillMaxWidth(),
                    text = description,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f)
                )

            }

        }

    }

}

@FluxPreview
@Composable
fun ErrorScreen_preview() {
    FluxTheme {
        ErrorScreen(
            message = stringResource(Res.string.oups_an_error_occured),
            description = "Error description",
            onBackButtonTap = {}
        )
    }
}