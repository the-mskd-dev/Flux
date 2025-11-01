package com.kaem.flux.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaem.flux.R
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.uppercaseFirstLetter

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