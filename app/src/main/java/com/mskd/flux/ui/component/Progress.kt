package com.mskd.flux.ui.component

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.ui.theme.Ui

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
                .padding(horizontal = Ui.Space.SMALL),
            contentAlignment = Alignment.Center
        ) {
            Text.Label.Medium(
                color = MaterialTheme.colorScheme.onTertiary,
                text = stringResource(R.string.watched)
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
        title = stringResource(R.string.reset_progress),
        onDismiss = onDismiss,
        onValidateLabel = stringResource(R.string.reset),
        onValidate = onValidate,
        content = {
            Text.Body.Large(
                text = stringResource(R.string.reset_progress_confirmation)
            )
        }
    )

}