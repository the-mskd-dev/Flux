package com.mskd.flux.ui.component.global

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.mskd.flux.R
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.utils.FluxPreview

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    message: String,
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

            Text.Body.Large(
                modifier = Modifier.fillMaxWidth(),
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

        }

    }

}

@FluxPreview
@Composable
fun ErrorScreen_preview() {
    AppTheme {
        ErrorScreen(
            message = stringResource(R.string.oups_an_error_occured),
            onBackButtonTap = {}
        )
    }
}