package com.mskd.flux.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.resolve
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoadingScreen(
    text: StringProvider? = null,
    progress: (() -> Float)? = null
) {

    val targetProgress = progress?.invoke()

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress ?: 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "LoadingProgressAnimation"
    )

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {

            LoadingIndicator(progress = progress)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {

                Text.Content.Body(
                    text = text?.resolve(),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (progress != null && animatedProgress > 0f) {
                    Text.Content.Body(
                        text = "${(animatedProgress * 100).roundToInt()}%",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            }

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