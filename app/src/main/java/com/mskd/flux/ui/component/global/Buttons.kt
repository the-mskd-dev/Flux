package com.mskd.flux.ui.component.global

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_arrow_down
import flux.shared.generated.resources.next_episode
import flux.shared.generated.resources.read_less
import flux.shared.generated.resources.read_more
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluxTextButton(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = ButtonDefaults.MediumContainerHeight,
    color: Color = MaterialTheme.colorScheme.primary,
    onTap: () -> Unit
) {

    TextButton(
        modifier = modifier.height(height),
        onClick = onTap,
        content = {
            Text.Button(
                text = text,
                color = color,
            )
        }
    )

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluxIconButton(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    onTap: () -> Unit,
    contentDescription: String
) {

    FloatingActionButton(
        modifier = modifier,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        onClick = onTap,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription
        )
    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CountDownButton(
    text: @Composable (Int) -> String,
    duration: Int = 10,
    onTap: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {

    var count by remember { mutableIntStateOf(duration) }
    val size = ButtonDefaults.MediumContainerHeight
    val shape = ButtonDefaults.shape
    val style = ButtonDefaults.textStyleFor(size).copy(
        fontFeatureSettings = "tnum"
    )

    LaunchedEffect(Unit) {
        while (count > 0) {
            delay(1.seconds)
            count -= 1
        }
        onTap()
    }

    Button(
        modifier = Modifier.heightIn(size),
        onClick = onTap,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColorFor(backgroundColor)
        ),
        shape = shape,
        contentPadding = ButtonDefaults.contentPaddingFor(size),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = FluxUI.Elevation.level3,
            pressedElevation = FluxUI.Elevation.level3,
            hoveredElevation = FluxUI.Elevation.level4,
            focusedElevation = FluxUI.Elevation.level3
        ),
        content = {

            Box {

                // Invisible text to avoid button size change
                Text.Button(
                    modifier = Modifier.clearAndSetSemantics { }, // To ignore TalkBack
                    text = text(duration),
                    color = Color.Transparent,
                )

                Text.Button(
                    text = text(count),
                    color = contentColorFor(backgroundColor),
                )

            }

        }
    )

}

@Composable
fun ReadMoreButton(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onTap: () -> Unit
) {

    val degrees by animateFloatAsState(if (isExpanded) 180f else 0f)

    IconButton(
        modifier = modifier.rotate(degrees),
        onClick = onTap
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
fun CountDownButton_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(FluxUI.Space.large)
        ) {
            CountDownButton(
                onTap = {  },
                text = { stringResource(Res.string.next_episode, it) }
            )
        }
    }
}

@Preview
@Composable
fun ReadMoreButton_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(FluxUI.Space.large),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
        ) {
            ReadMoreButton(
                onTap = {  },
                isExpanded = true
            )
            ReadMoreButton(
                onTap = {  },
                isExpanded = false
            )
        }
    }
}