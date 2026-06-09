package com.mskd.flux.ui.component.global

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluxButton(
    modifier: Modifier = Modifier,
    text: String,
    height: Dp = ButtonDefaults.MediumContainerHeight,
    shape: Shape = MaterialTheme.shapes.medium,
    autoSize: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    border: BorderStroke? = null,
    icon: ImageVector? = null,
    onTap: () -> Unit
) {

    val typography = ButtonDefaults.textStyleFor(height)
    Button(
        modifier = modifier.height(height),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(),
        shape = shape,
        border = border,
        contentPadding = ButtonDefaults.contentPaddingFor(height),
        onClick = onTap
    ) {

        icon?.let {
            AnimatedContent(
                targetState = it,
                label = "FluxButton icon animation"
            ) { state ->
                Icon(
                    modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)),
                    imageVector = state,
                    tint = textColor,
                    contentDescription = null,
                )
            }

            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))

        }

        AnimatedContent(
            targetState = text,
            label = "FluxButton text animation"
        ) { state ->
            if (autoSize) {

                var fontSize by remember { mutableStateOf(typography.fontSize) }
                var readyToDraw by remember { mutableStateOf(false) }

                Text(
                    modifier = Modifier.drawWithContent {
                        if (readyToDraw) drawContent()
                    },
                    text = state,
                    fontSize = fontSize,
                    maxLines = 1,
                    fontWeight = typography.fontWeight,
                    softWrap = false,
                    onTextLayout = {
                        if (it.didOverflowWidth)
                            fontSize = fontSize.times(.95)
                        else
                            readyToDraw = true
                    }
                )
            } else {
                Text(
                    text = state,
                    color = textColor,
                    style = typography
                )
            }
        }

    }

}

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
            Text(
                text = text,
                color = color,
                style = ButtonDefaults.textStyleFor(height)
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
            defaultElevation = Ui.Elevation.level3,
            pressedElevation = Ui.Elevation.level3,
            hoveredElevation = Ui.Elevation.level4,
            focusedElevation = Ui.Elevation.level3
        ),
        content = {

            Box {

                // Invisible text to avoid button size change
                Text.Adaptive(
                    modifier = Modifier.clearAndSetSemantics { }, // To ignore TalkBack
                    text = text(duration),
                    color = Color.Transparent,
                    style = style
                )

                Text.Adaptive(
                    text = text(count),
                    color = contentColorFor(backgroundColor),
                    style = style
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
            painter = painterResource(R.drawable.ic_arrow_down),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = stringResource(if (isExpanded) R.string.read_less else R.string.read_more)
        )
    }

}

@Preview
@Composable
fun CountDownButton_Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Ui.Space.large)
        ) {
            CountDownButton(
                onTap = {  },
                text = { stringResource(R.string.next_episode, it) }
            )
        }
    }
}

@Preview
@Composable
fun ReadMoreButton_Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Ui.Space.large),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.large)
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