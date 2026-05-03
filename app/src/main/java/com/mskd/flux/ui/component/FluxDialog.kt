package com.mskd.flux.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R

/**
 * Simple AlertDialog with Cancel and Validate buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDialog(
    onDismiss: () -> Unit,
    onDismissLabel: String = stringResource(R.string.cancel),
    onValidate: (() -> Unit)? = null,
    onValidateLabel: String = stringResource(R.string.validate),
    title: String? = null,
    content: @Composable () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onValidate?.invoke() },
                content = {
                    Text.Label.Large(text = onValidateLabel)
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text.Label.Large(text = onDismissLabel)
                }
            )
        },
        title = { Text.Headline.Small(text = title) },
        text = content
    )

}