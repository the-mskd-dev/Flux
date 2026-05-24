package com.mskd.flux.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mskd.flux.screens.settings.SettingsIntent
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.uppercaseFirstLetter

@Composable
fun <T, R> FluxOptionsDialog(
    state: FluxOptionsDialogState<T, R>,
    onValidate: (R) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedValue by remember { mutableStateOf(state.currentValue) }

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { onValidate(state.applyValue.invoke(selectedValue)) },
        title = stringResource(state.titleResId),
        content = {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                state.options.forEach { option ->

                    Row(
                        modifier = Modifier
                            .clickable { selectedValue = option.value  }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
                    ) {

                        RadioButton(
                            selected = selectedValue == option.value,
                            onClick = { selectedValue = option.value }
                        )

                        val value = option.label
                        Text.Body.Large(
                            modifier = Modifier.weight(1f),
                            text = value.uppercaseFirstLetter(),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

                }

            }

        }
    )

}

data class FluxOptionsDialogState<T, out R>(
    val titleResId: Int,
    val currentValue: T,
    val options: List<FluxOptionsDialogItem<T>>,
    val applyValue: (T) -> R
)

data class FluxOptionsDialogItem<T>(
    val value: T,
    val label: String,
)