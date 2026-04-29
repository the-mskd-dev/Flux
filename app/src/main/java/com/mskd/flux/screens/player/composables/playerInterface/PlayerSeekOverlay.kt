package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.constraintlayout.compose.layoutId
import com.mskd.flux.R
import com.mskd.flux.screens.player.PlayerUiState
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.LandscapePreview

@Composable
fun PlayerSeekOverlay(
    layoutIdLeft: String,
    layoutIdRight: String,
    seekOverlay: () -> PlayerUiState.SeekOverlay?
) {

    val overlay = seekOverlay()

    Box(
        modifier = Modifier.layoutId(layoutIdLeft),
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = overlay?.type == PlayerUiState.SeekOverlay.Type.REWIND,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            label = "Visibility left seek overlay"
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Ui.Space.LARGE),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerSeekOverlayIcon(
                    painter = painterResource(R.drawable.ic_rewind),
                    offsetX = { fullWidth -> fullWidth },
                    label = "Left arrow"
                )
                PlayerSeekOverlayText(amount = overlay?.amount?.let { "-$it" })
            }
        }

    }

    Box(
        modifier = Modifier.layoutId(layoutIdRight),
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = overlay?.type == PlayerUiState.SeekOverlay.Type.FORWARD,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            label = "Visibility right seek overlay"
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Ui.Space.LARGE),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerSeekOverlayText(amount = overlay?.amount?.let { "+$it" })
                PlayerSeekOverlayIcon(
                    painter = painterResource(R.drawable.ic_forward),
                    offsetX = { fullWidth -> -fullWidth },
                    label = "Right arrow"
                )
            }
        }

    }

}

@Composable
fun AnimatedVisibilityScope.PlayerSeekOverlayIcon(
    painter: Painter,
    offsetX: (Int) -> Int,
    label: String
) {

    Box(
        modifier = Modifier
            .animateEnterExit(
                enter = slideInHorizontally(
                    spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetX = offsetX
                ),
                exit = slideOutHorizontally(targetOffsetX = offsetX),
                label = "$label anim"
            ),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            painter = painter,
            contentDescription = label,
            tint = Color.Unspecified
        )

    }

}

@Composable
fun PlayerSeekOverlayText(amount: String?) {
    AnimatedContent(
        transitionSpec = {
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) togetherWith scaleOut()
        },
        targetState = amount,
        label = "SeekOverlayText change"
    ) { text ->
        Box(modifier = Modifier.padding(all = Ui.Space.SMALL)) {
            Text.Adaptive(
                text = text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    drawStyle = Stroke(
                        miter = 10f,
                        width = 6f,
                        join = StrokeJoin.Round
                    ),
                    color = Color.Black
                )
            )
            Text.Headline.Small(
                text = text,
                color = Color.White,
            )
        }
    }

}

@LandscapePreview
@Composable
fun PlayerSeekOverlay_Preview() {
    AppTheme {
        Surface(color = Color.Gray) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerSeekOverlay(
                    layoutIdLeft = "",
                    layoutIdRight = "",
                    seekOverlay = { PlayerUiState.SeekOverlay(amount = 10, type = PlayerUiState.SeekOverlay.Type.REWIND) }
                )
                PlayerSeekOverlay(
                    layoutIdLeft = "",
                    layoutIdRight = "",
                    seekOverlay = { PlayerUiState.SeekOverlay(amount = 10, type = PlayerUiState.SeekOverlay.Type.FORWARD) }
                )
            }

        }
    }
}