package com.kaem.flux.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaem.flux.R
import com.kaem.flux.ui.theme.Ui


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDialog(
    show: Boolean,
    title: String? = null,
    text: String? = null,
    hideButtons: Boolean = false,
    cancelText: String = stringResource(android.R.string.cancel),
    validateText: String = stringResource(R.string.validate),
    onDismissRequest: () -> Unit,
    onValidate: (() -> Unit)? = null
) {

    FluxDialog(
        show = show,
        cancelText = cancelText,
        validateText = validateText,
        hideButtons = hideButtons,
        onDismissRequest = onDismissRequest,
        onValidate = onValidate,
        content = {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                BoldText(text = title)

                MediumText(text = text)

            }

        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxDialog(
    show: Boolean,
    hideButtons: Boolean = false,
    cancelText: String = stringResource(android.R.string.cancel),
    validateText: String = stringResource(R.string.validate),
    onDismissRequest: () -> Unit,
    onValidate: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    if (show) {
        BasicAlertDialog(onDismissRequest = onDismissRequest) {

            Card(shape = Ui.Shape.RoundedCorner) {

                Column(modifier = Modifier.padding(Ui.Space.MEDIUM)) {

                    content()

                    if (!hideButtons) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = Ui.Space.LARGE),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM, Alignment.End)
                        ) {

                            TextButton(onClick = onDismissRequest) {
                                Text(cancelText)
                            }

                            onValidate?.let {
                                TextButton(onClick = it) {
                                    Text(validateText)
                                }
                            }

                        }
                    }
                }
            }

        }
    }

}