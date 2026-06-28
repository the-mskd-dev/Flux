package com.mskd.flux.screens.customization.composables

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.component.global.FluxDialog
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.corners
import flux.shared.generated.resources.corners_desc
import flux.shared.generated.resources.large
import flux.shared.generated.resources.medium
import flux.shared.generated.resources.small
import org.jetbrains.compose.resources.stringResource

@Composable
fun CornersDialog(
    value: Int,
    onValidate: (Int) -> Unit,
    onDismiss: () -> Unit,
) {

    val options = mapOf(
        8 to Res.string.small,
        12 to Res.string.medium,
        16 to Res.string.large
    )
    var currentValue by remember { mutableIntStateOf(value) }
    val animatedValue by animateIntAsState(currentValue)

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { onValidate(currentValue) },
        title = stringResource(Res.string.corners),
        content = {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
            ) {

                Text.Body.Medium(
                    text = stringResource(Res.string.corners_desc)
                )

                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween
                    )
                ) {
                    options.forEach { (value, text) ->
                        ToggleButton(
                            checked = currentValue == value,
                            onCheckedChange = { currentValue = value },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Text.Body.Small(text = stringResource(text) )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(.5f)
                        .aspectRatio(4f/3f)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(animatedValue.dp)
                        ),

                )

            }

        }
    )

}

@Preview
@Composable
fun CornersDialog_Preview() {
    FluxTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            CornersDialog(
                value = 12,
                onValidate = {},
                onDismiss = {}
            )

        }
    }
}