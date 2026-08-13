package com.mskd.flux.ui.component.global

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.screens.customization.composables.ColorItem
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.resolve
import com.mskd.flux.utils.extensions.uppercaseFirstLetter
import org.jetbrains.compose.resources.stringResource

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
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
            ) {

                state.options.forEach { option ->

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedValue = option.value  }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
                    ) {

                        RadioButton(
                            selected = selectedValue == option.value,
                            onClick = { selectedValue = option.value }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                        ) {

                            ColorItem(option.color)

                            val value = option.label.resolve()
                            Text.Content.Body(
                                modifier = Modifier.weight(1f),
                                text = value.uppercaseFirstLetter(),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                        }

                    }

                }

            }

        }
    )

}