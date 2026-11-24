package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.ExpressiveRow
import com.mskd.flux.ui.components.buttons.expressivePress
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_forward_10
import flux.shared.generated.resources.ic_forward_30
import flux.shared.generated.resources.ic_forward_5
import flux.shared.generated.resources.ic_pause
import flux.shared.generated.resources.ic_play
import flux.shared.generated.resources.ic_rewind_10
import flux.shared.generated.resources.ic_rewind_30
import flux.shared.generated.resources.ic_rewind_5
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerControlButtons(
    modifier: Modifier,
    isPlaying: Boolean,
    rewindAndForward: Pair<Int, Int>,
    slide: () -> Float,
    sendIntent: (PlayerIntent) -> Unit
) {

    val [rewind, forward] = rewindAndForward

    ExpressiveRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {

        MediumFloatingActionButton(
            modifier = Modifier
                .playerControlSlide(slideSign = 1f, slide = slide)
                .expressiveTag("rewind")
                .expressiveSideBounce(rightTag = "play")
                .expressivePress(),
            onClick = { sendIntent(PlayerIntent.OnFastRewind) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            val icon = when (rewind) {
                5 -> Res.drawable.ic_rewind_5
                10 -> Res.drawable.ic_rewind_10
                else -> Res.drawable.ic_rewind_30
            }
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                painter = painterResource(icon),
                contentDescription = "rewind button"
            )
        }

        LargeExtendedFloatingActionButton(
            modifier = Modifier
                .expressiveTag("play")
                .expressiveSideBounce(leftTag = "rewind", rightTag = "forward")
                .expressivePress(),
            onClick = { sendIntent(PlayerIntent.TogglePlayButton) },
            shape = FloatingActionButtonDefaults.largeExtendedFabShape
        ) {
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                painter = painterResource(if (isPlaying) Res.drawable.ic_pause else Res.drawable.ic_play),
                contentDescription = "play button"
            )
        }

        MediumFloatingActionButton(
            modifier = Modifier
                .playerControlSlide(slideSign = -1f, slide = slide)
                .expressiveTag("forward")
                .expressiveSideBounce(leftTag = "play")
                .expressivePress(),
            onClick = { sendIntent(PlayerIntent.OnFastForward) },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ) {
            val icon = when (forward) {
                5 -> Res.drawable.ic_forward_5
                10 -> Res.drawable.ic_forward_10
                else -> Res.drawable.ic_forward_30
            }
            Icon(
                modifier = Modifier.size(FloatingActionButtonDefaults.MediumIconSize),
                painter = painterResource(icon),
                contentDescription = "forward button"
            )
        }

    }

}

private fun Modifier.playerControlSlide(slideSign: Float, slide: () -> Float): Modifier =
    graphicsLayer { translationX = slideSign * size.width * (1f - slide()) }

@FluxPreview
@Composable
fun PlayerControlButtons_Preview() {

    FluxTheme {
        AnimatedContent(targetState = true) {
            PlayerControlButtons(
                modifier = Modifier,
                isPlaying = it,
                rewindAndForward = 5 to 10,
                slide = { 1f },
                sendIntent = {}
            )
        }
    }
}
