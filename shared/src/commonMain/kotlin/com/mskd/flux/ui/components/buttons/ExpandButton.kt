package com.mskd.flux.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.motion.expressivePress
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_arrow_down
import flux.shared.generated.resources.read_less
import flux.shared.generated.resources.read_more
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpandButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit
) {

    val degrees by animateFloatAsState(if (isExpanded) 180f else 0f)

    IconButton(
        modifier = modifier.expressivePress(),
        shape = MaterialShapes.Pill.toShape(),
        colors = IconButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledContainerColor = Design.Button.disabledContainer(),
            disabledContentColor = Design.Button.disabledContent()
        ),
        onClick = onClick
    ) {
        Icon(
            modifier = modifier.rotate(degrees),
            painter = painterResource(Res.drawable.ic_arrow_down),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(if (isExpanded) Res.string.read_less else Res.string.read_more)
        )
    }

}

@Preview
@Composable
fun ExpandButton_Preview() {

    var expanded by remember { mutableStateOf(false) }

    FluxThemePreview {
        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Column {

                ExpandButton(
                    isExpanded = expanded,
                    onClick = { expanded = !expanded}
                )

            }

        }
    }
}