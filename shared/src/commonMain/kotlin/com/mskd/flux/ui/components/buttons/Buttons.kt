package com.mskd.flux.ui.components.buttons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.text.Text
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_arrow_down
import flux.shared.generated.resources.read_less
import flux.shared.generated.resources.read_more
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FluxTextButton(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = ButtonDefaults.MediumContainerHeight,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {

    TextButton(
        modifier = modifier.height(height),
        onClick = onClick,
        content = {
            Text.Button.Default(
                text = text,
                color = color,
            )
        }
    )

}

@Composable
fun FluxIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
    contentDescription: String
) {

    FloatingActionButton(
        modifier = modifier,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription
        )
    }

}

@Composable
fun ReadMoreButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit
) {

    val degrees by animateFloatAsState(if (isExpanded) 180f else 0f)

    IconButton(
        modifier = modifier.rotate(degrees),
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_down),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(if (isExpanded) Res.string.read_less else Res.string.read_more)
        )
    }

}

@Preview
@Composable
fun ReadMoreButton_Preview() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(Design.Space.large),
        verticalArrangement = Arrangement.spacedBy(Design.Space.large)
    ) {
        ReadMoreButton(
            onClick = {  },
            isExpanded = true
        )
        ReadMoreButton(
            onClick = {  },
            isExpanded = false
        )
    }
}