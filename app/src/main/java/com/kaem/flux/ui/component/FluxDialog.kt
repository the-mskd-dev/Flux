package com.kaem.flux.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    cancelText: String = stringResource(R.string.no),
    validateText: String = stringResource(R.string.yes),
    onDismissRequest: () -> Unit,
    onValidate: (() -> Unit)? = null
) {

    if (show) {
        BasicAlertDialog(onDismissRequest = onDismissRequest) {

            Card(shape = Ui.Shape.RoundedCorner) {

                Column(
                    modifier = Modifier.padding(Ui.Space.MEDIUM),
                    verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
                ) {

                    BoldText(text = title)

                    LightText(text = text)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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