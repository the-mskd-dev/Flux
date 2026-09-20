package com.mskd.flux.ui.component.global

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        Card(
            shape = FluxUI.shapes.corners,
            modifier = Modifier.imePadding()
        ) {

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(FluxUI.Space.large),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {

                Text.Content.Title(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface
                )

                content()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {

                    TextButton(onClick = onDismiss) { Text.Button.Default(text = onDismissLabel) }

                    onValidate?.let {
                        TextButton(onClick = it) { Text.Button.Default(text = onValidateLabel) }
                    }

                }

            }

        }

    }

}