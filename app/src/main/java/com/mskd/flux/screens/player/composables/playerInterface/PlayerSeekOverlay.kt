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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.constraintlayout.compose.layoutId
import com.mskd.flux.features.player.presentation.PlayerUiContent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.LandscapePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_forward
import flux.shared.generated.resources.ic_rewind
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerSeekOverlay(
    layoutIdLeft: String,
    layoutIdRight: String,
    seekOverlay: () -> PlayerUiContent.SeekOverlay?
) {

    val overlay = seekOverlay()

    Box(
        modifier = Modifier.layoutId(layoutIdLeft),
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = overlay?.type == PlayerUiContent.SeekOverlay.Type.REWIND,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            label = "Visibility left seek overlay"
        ) {
            Row(
                modifier = Modifier.padding(horizontal = FluxUI.Space.large),
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerSeekOverlayIcon(
                    painter = painterResource(Res.drawable.ic_rewind),
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
            visible = overlay?.type == PlayerUiContent.SeekOverlay.Type.FORWARD,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            label = "Visibility right seek overlay"
        ) {
            Row(
                modifier = Modifier.padding(horizontal = FluxUI.Space.large),
                horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerSeekOverlayText(amount = overlay?.amount?.let { "+$it" })
                PlayerSeekOverlayIcon(
                    painter = painterResource(Res.drawable.ic_forward),
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
        Box(modifier = Modifier.padding(all = FluxUI.Space.small)) {
            Text.Content.Body(
                text = text,
                color = Color.White,
            )
        }
    }

}

@LandscapePreview
@Composable
fun PlayerSeekOverlay_Preview() {
    FluxTheme {
        Surface(color = Color.Gray) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerSeekOverlay(
                    layoutIdLeft = "",
                    layoutIdRight = "",
                    seekOverlay = { PlayerUiContent.SeekOverlay(amount = 10, type = PlayerUiContent.SeekOverlay.Type.REWIND) }
                )
                PlayerSeekOverlay(
                    layoutIdLeft = "",
                    layoutIdRight = "",
                    seekOverlay = { PlayerUiContent.SeekOverlay(amount = 10, type = PlayerUiContent.SeekOverlay.Type.FORWARD) }
                )
            }

        }
    }
}