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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CountDownButton(
    text: @Composable (Int) -> String,
    duration: Int = 10,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {

    var count by remember { mutableIntStateOf(duration) }
    val size = ButtonDefaults.MediumContainerHeight
    val shape = ButtonDefaults.shape

    LaunchedEffect(Unit) {
        while (count > 0) {
            delay(1.seconds)
            count -= 1
        }
        onClick()
    }

    Button(
        modifier = Modifier.heightIn(size),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColorFor(backgroundColor)
        ),
        shape = shape,
        contentPadding = ButtonDefaults.contentPaddingFor(size),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 6.dp,
            hoveredElevation = 4.dp,
            focusedElevation = 6.dp
        ),
        content = {

            Box {

                // Invisible text to avoid button size change
                Text.Button.Default(
                    modifier = Modifier.clearAndSetSemantics { }, // To ignore TalkBack
                    text = text(duration),
                    color = Color.Transparent,
                )

                Text.Button.Default(
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
fun CountDownButton_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(FluxUI.Space.large)
        ) {
            CountDownButton(
                onClick = {  },
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
                onClick = {  },
                isExpanded = true
            )
            ReadMoreButton(
                onClick = {  },
                isExpanded = false
            )
        }
    }
}