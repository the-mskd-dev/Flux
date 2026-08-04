package com.mskd.flux.ui.component.global

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.reset
import flux.shared.generated.resources.reset_progress
import flux.shared.generated.resources.reset_progress_confirmation
import flux.shared.generated.resources.watched
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProgressStatusBar(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,
    isVisible: Boolean,
    progress: () -> Float
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isVisible
    ) {

        LinearProgressIndicator(
            modifier = Modifier
                .height(8.dp)
                .fillMaxWidth(),
            color = color,
            progress = progress,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )

    }
}


@Composable
fun ProgressStatusChip(
    modifier: Modifier = Modifier,
    isWatched: Boolean
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = isWatched
    ) {
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.tertiary)
                .height(32.dp)
                .widthIn(min = 40.dp)
                .padding(horizontal = FluxUI.Space.small),
            contentAlignment = Alignment.Center
        ) {
            Text.Button.Chip(
                color = MaterialTheme.colorScheme.onTertiary,
                text = stringResource(Res.string.watched)
            )
        }
    }

}

@Composable
fun ResetProgressDialog(
    onValidate: () -> Unit,
    onDismiss: () -> Unit
) {

    FluxDialog(
        title = stringResource(Res.string.reset_progress),
        onDismiss = onDismiss,
        onValidateLabel = stringResource(Res.string.reset),
        onValidate = onValidate,
        content = {
            Text.Content.Body(
                text = stringResource(Res.string.reset_progress_confirmation)
            )
        }
    )

}