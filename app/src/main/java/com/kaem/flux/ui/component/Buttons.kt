package com.kaem.flux.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FluxButton(
    modifier: Modifier = Modifier,
    text: String,
    height: Dp = ButtonDefaults.MediumContainerHeight,
    shape: Shape = Ui.Shape.Corner.Medium,
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

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    onTap: () -> Unit
) {

    Box(
        modifier = modifier
            .statusBarsPadding()
            .clickable { onTap() }
            .size(50.dp)
            .clip(shape = CircleShape)
            .padding(Ui.Space.EXTRA_SMALL),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            tint = tint,
            contentDescription = "back button"
        )

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
fun ProgressButton(
    text: @Composable (Dp) -> Unit,
    icon: @Composable ((Dp) -> Unit)? = null,
    duration: Long = 5000L,
    onFinish: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    progressColor: Color = MaterialTheme.colorScheme.tertiary,
) {

    var progress by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "BackgroundProgressText",
        animationSpec = tween(
            durationMillis = (duration * 0.11f).roundToInt(),
            easing = LinearEasing
        )
    )

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            progress += 0.1f
            delay(duration / 10)
        }
        onFinish()
    }

    val buttonSize = ButtonDefaults.MediumContainerHeight
    val buttonShape = ButtonDefaults.shape

    Surface(
        shape = buttonShape,
        shadowElevation = Ui.Elevation.Level3
    ) {

        Row(
            modifier = Modifier
                .heightIn(buttonSize)
                .background(backgroundColor)
                .drawBehind {
                    val width = size.width * animatedProgress

                    val outline = RectangleShape.createOutline(
                        size = size.copy(width = width),
                        layoutDirection = layoutDirection,
                        density = density
                    )

                    drawOutline(
                        outline = outline,
                        color = progressColor
                    )
                }
                .padding(ButtonDefaults.contentPaddingFor(buttonSize)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.iconSpacingFor(buttonSize))
        ) {

            icon?.invoke(buttonSize)

            text.invoke(buttonSize)

        }

    }

}

@Preview
@Composable
fun ProgressButton_Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Ui.Space.LARGE)
        ) {
            ProgressButton(
                onFinish = {  },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        tint = MaterialTheme.colorScheme.onTertiary,
                        contentDescription = "next episode button"
                    )
                },
                text = {

                    Text.Title.Large(
                        text = stringResource(R.string.next_episode).uppercase(),
                        color = MaterialTheme.colorScheme.onTertiary
                    )

                }
            )
        }
    }
}