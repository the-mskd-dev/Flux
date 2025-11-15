package com.kaem.flux.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.kaem.flux.R

/**
 * Simple AlertDialog with Cancel and Validate buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDialog(
    onDismiss: () -> Unit,
    onValidate: (() -> Unit)? = null,
    title: String? = null,
    content: @Composable () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onValidate?.invoke(); onDismiss() },
                content = {
                    Text.Label.Large(text = stringResource(R.string.validate))
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text.Label.Large(text = stringResource(R.string.cancel))
                }
            )
        },
        title = { Text.Headline.Small(text = title) },
        text = content
    )

}