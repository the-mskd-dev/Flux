package com.mskd.flux.screens.customization.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun ItemsPerRowDialog(
    value: Int,
    title: String,
    description: String,
    onValidate: (Int) -> Unit,
    onDismiss: () -> Unit,
) {

    var currentValue by remember { mutableIntStateOf(value) }

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { onValidate(currentValue) },
        title = title,
        content = {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
            ) {

                Text.Card.Body(description)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FluxUI.Space.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
                ) {

                    Text.Card.Body("2")

                    Slider(
                        modifier = Modifier.weight(1f),
                        valueRange = 2f..5f,
                        steps = 2,
                        value = currentValue.toFloat(),
                        onValueChange = { currentValue = it.toInt() },
                    )

                    Text.Card.Body("5")

                }

            }

        }
    )

}