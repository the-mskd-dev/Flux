package com.mskd.flux.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingScreen(
    text: String? = null,
    progress: (() -> Float)? = null
) {

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            LoadingIndicator(progress = progress)

            Text.Content.Body(
                text = text,
                color = MaterialTheme.colorScheme.onSurface
            )

        }


    }

}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingIndicator(progress: (() -> Float)?) {

    if (progress == null) {
        CircularWavyProgressIndicator()
        return
    }

    val isDeterminate by remember(progress) {
        derivedStateOf { progress() > 0f }
    }

    if (isDeterminate) {
        CircularWavyProgressIndicator(progress = progress)
    } else {
        CircularWavyProgressIndicator()
    }

}