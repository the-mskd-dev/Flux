package com.kaem.flux.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kaem.flux.ui.theme.FluxFontSize

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    message: String,
    onBackButtonTap: (() -> Unit)? = null
) {

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        contentAlignment = Alignment.TopStart
    ) {

        onBackButtonTap?.let {
            BackButton(onTap = onBackButtonTap)
        }

        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            text = message,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = FluxFontSize.MEDIUM,
            textAlign = TextAlign.Center,
            lineHeight = FluxFontSize.MEDIUM
        )

    }

}