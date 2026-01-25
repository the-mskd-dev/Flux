package com.kaem.flux.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kaem.flux.R
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressText(
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

    val buttonSize = ButtonDefaults.LargeContainerHeight
    val buttonShape = ButtonDefaults.shape
    Button(
        modifier = Modifier
            .heightIn(buttonSize)
            .drawBehind {
                val width = size.width * animatedProgress

                val outline = buttonShape.createOutline(
                    size = size.copy(width = width),
                    layoutDirection = layoutDirection,
                    density = density
                )

                drawOutline(
                    outline = outline,
                    color = progressColor
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
        ),
        contentPadding = ButtonDefaults.contentPaddingFor(buttonSize),
        onClick = onFinish
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.iconSpacingFor(buttonSize))
        ) {

            icon?.invoke(buttonSize)

            text.invoke(buttonSize)

        }

    }

    Surface(
        shape = Ui.Shape.Corner.Medium,
        tonalElevation = Ui.Elevation.Level3,
        shadowElevation = Ui.Elevation.Level3
    ) {



    }

}

@Preview
@Composable
fun ProgressText_Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(Ui.Space.LARGE)
        ) {
            ProgressText(
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