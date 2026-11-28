package com.mskd.flux.ui.components.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.motion.Motion
import com.mskd.flux.ui.text.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_play
import org.jetbrains.compose.resources.painterResource

@Composable
fun FluxButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val buttonHeight = ButtonDefaults.MediumContainerHeight

    val containerColor = when {
        !enabled -> Design.Button.disabledContainer()
        checked -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    val contentColor = when {
        !enabled -> Design.Button.disabledContent()
        checked -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onPrimary
    }

    ExpressiveButton(
        modifier = modifier.height(buttonHeight),
        onClick = onClick,
        active = checked,
        enabled = enabled,
        containerColor = containerColor,
        contentColor = contentColor,
        contentPadding = PaddingValues(
            vertical = Design.Space.medium,
            horizontal = Design.Space.large,
        ),
        contentSpacing = ButtonDefaults.iconSpacingFor(buttonHeight),
    ) {
        AnimatedVisibility(
            visible = icon != null,
            enter = Motion.buttonContentEnter(),
            exit = ExitTransition.None
        ) {
            icon?.let {
                Icon(
                    modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight)),
                    painter = it,
                    contentDescription = text
                )
            }
        }

        androidx.compose.material3.Text(
            text = text,
            style = ButtonDefaults.textStyleFor(buttonHeight)
        )
    }
}

@Preview
@Composable
fun FluxButton_FullPreview() {

    var checked by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(true) }
    var showIcon by remember { mutableStateOf(true) }

    FluxThemePreview {
        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )
                    Text.Content.Body("Checked")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                    Text.Content.Body("Enabled")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = showIcon,
                        onCheckedChange = { showIcon = it }
                    )
                    Text.Content.Body("Icon")
                }

                FluxButton(
                    text = "ExpressiveButton",
                    icon = if (showIcon) painterResource(Res.drawable.ic_play) else null,
                    checked = checked,
                    enabled = enabled,
                    onClick = {}
                )

            }

        }
    }
}

@Preview
@Composable
fun FluxButton_Preview() {
    MaterialTheme {
        FluxButton(
            text = "ExpressiveButton",
            icon = painterResource(Res.drawable.ic_play),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FluxButton_Checked_Preview() {
    MaterialTheme {
        FluxButton(
            text = "Checked",
            icon = painterResource(Res.drawable.ic_play),
            checked = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FluxButton_Disabled_Preview() {
    MaterialTheme {
        FluxButton(
            text = "Disabled",
            icon = painterResource(Res.drawable.ic_play),
            enabled = false,
            onClick = {}
        )
    }
}