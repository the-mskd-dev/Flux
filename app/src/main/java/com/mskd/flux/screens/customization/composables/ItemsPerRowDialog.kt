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
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.screens.customization.CustomizationIntent
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.Ui

@Composable
fun ItemsPerRowDialog(
    value: Int,
    sendIntent: (CustomizationIntent) -> Unit
) {

    var currentValue by remember { mutableIntStateOf(value) }

    FluxDialog(
        onDismiss = { sendIntent(CustomizationIntent.HideDialog) },
        onValidate = { sendIntent(CustomizationIntent.SetItemsPerRowValue(count = currentValue)) },
        title = stringResource(R.string.items_per_row),
        content = {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                Text.Body.Medium(stringResource(R.string.items_per_row_desc))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Ui.Space.SMALL),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
                ) {

                    Text.Body.Medium("2")

                    Slider(
                        modifier = Modifier.weight(1f),
                        valueRange = 2f..5f,
                        steps = 2,
                        value = currentValue.toFloat(),
                        onValueChange = { currentValue = it.toInt() },
                    )

                    Text.Body.Medium("5")

                }

            }

        }
    )

}