package com.mskd.flux.ui.component.global

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.cancel
import flux.shared.generated.resources.validate
import org.jetbrains.compose.resources.stringResource

/**
 * Simple AlertDialog with Cancel and Validate buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDialog(
    onDismiss: () -> Unit,
    onDismissLabel: String = stringResource(Res.string.cancel),
    onValidate: (() -> Unit)? = null,
    onValidateLabel: String = stringResource(Res.string.validate),
    title: String? = null,
    content: @Composable () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            onValidate?.let {
                TextButton(
                    onClick = { it() },
                    content = {
                        Text.Button.Default(text = onValidateLabel)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text.Button.Default(text = onDismissLabel)
                }
            )
        },
        title = { Text.Content.Title(text = title) },
        shape = FluxUI.shapes.corners,
        text = content
    )

}