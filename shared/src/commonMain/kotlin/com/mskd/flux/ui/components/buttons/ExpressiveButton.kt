package com.mskd.flux.ui.components.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.expressive.expressivePress
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_play
import flux.shared.generated.resources.ic_replay
import org.jetbrains.compose.resources.painterResource

@Composable
fun ExpressiveButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
) {

    val buttonHeight = ButtonDefaults.MediumContainerHeight

    ToggleButton(
        modifier = modifier
            .expressivePress(enabled = enabled)
            .height(buttonHeight),
        checked = checked,
        enabled = enabled,
        onCheckedChange = { onClick() },
        colors = ToggleButtonDefaults.colors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            checkedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            checkedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shapes = ToggleButtonShapes(
            shape = CircleShape,
            pressedShape = MaterialTheme.shapes.medium,
            checkedShape = MaterialTheme.shapes.small,
        ),
        content = {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.iconSpacingFor(buttonHeight))
            ) {

                icon?.let {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight)),
                        painter = it,
                        contentDescription = "Play button"
                    )
                }

                androidx.compose.material3.Text(
                    text = text,
                    style = ButtonDefaults.textStyleFor(buttonHeight)
                )
            }

        }
    )

}

@Preview
@Composable
fun ExpressiveButton_Preview() {
    MaterialTheme {
        ExpressiveButton(
            text = "ExpressiveButton",
            icon = painterResource(Res.drawable.ic_play),
            onClick = {}
        )
    }
}