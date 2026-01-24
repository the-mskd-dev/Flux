package com.kaem.flux.screens.player.composables.playerInterface

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.delay

@Composable
fun PlayerNextEpisode(
    modifier: Modifier,
    episode: Episode?,
    sendIntent: (PlayerIntent) -> Unit
) {

    AnimatedVisibility(
        modifier = modifier.clickable { episode?.let { sendIntent(PlayerIntent.PlayNextEpisode(it)) } },
        visible = episode != null,
        enter = fadeIn() + slideInHorizontally { it / 2 },
        exit = fadeOut() + slideOutHorizontally { it / 2 }
    ) {

        episode ?: return@AnimatedVisibility

        var progress by remember { mutableFloatStateOf(0f) }
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            label = "BackgroundProgress",
            animationSpec = tween(
                durationMillis = 550,
                easing = LinearEasing
            )
        )

        LaunchedEffect(Unit) {
            while (progress < 1f) {
                progress += 0.1f
                delay(500L)
            }
            sendIntent(PlayerIntent.PlayNextEpisode(episode))
        }

        ProgressiveText(progress = animatedProgress)

    }

}

@Composable
fun ProgressiveText(
    progress: Float,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    progressColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary
) {

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    Row(
        modifier = Modifier
            .clip(Ui.Shape.Corner.Medium)
            .background(backgroundColor)
            .drawBehind {
                val width = size.width * progress

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
            .padding(Ui.Space.LARGE),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        Icon(
            painter = painterResource(R.drawable.skip_next),
            tint = contentColor,
            contentDescription = "next episode button"
        )

        Text.Title.Large(
            text = stringResource(R.string.next_episode).uppercase(),
            color = contentColor
        )

    }

}

@Preview
@Composable
fun PlayerNextEpisode_Preview() {
    AppTheme {
        Column {
            ProgressiveText(progress = .4f)
        }
    }
}