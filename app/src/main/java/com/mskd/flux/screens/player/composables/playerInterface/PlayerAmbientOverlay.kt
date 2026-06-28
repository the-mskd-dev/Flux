package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskd.flux.screen.player.PlayerUiContent
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.LandscapePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_brightness
import flux.shared.generated.resources.ic_volume
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerAmbientOverlay(
    modifier: Modifier = Modifier,
    ambientOverlay: () -> PlayerUiContent.AmbientOverlay?) {

    val overlay = ambientOverlay()

    var value by remember { mutableIntStateOf(0) }
    if (overlay?.value != null) {
        value = overlay.value
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = overlay != null,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = scaleOut(),
        label = "Visibility ambient overlay"
    ) {

        Column(
            modifier = Modifier
                .clip(shape = FluxUI.shapes.corners)
                .background(color = Color.Black.copy(alpha = .5f))
                .padding(vertical = FluxUI.Space.medium, horizontal = FluxUI.Space.large),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Crossfade(
                targetState = overlay?.type,
                label = "Crossfade ambient overlay"
            ) {
                when (it) {
                    PlayerUiContent.AmbientOverlay.Type.BRIGHTNESS ->
                        Icon(
                            modifier = Modifier.size(size = 36.dp),
                            painter = painterResource(Res.drawable.ic_brightness),
                            tint = Color.White,
                            contentDescription = "icon brightness"
                        )
                    else ->
                        Icon(
                            modifier = Modifier.size(size = 36.dp),
                            painter = painterResource(Res.drawable.ic_volume),
                            tint = Color.White,
                            contentDescription = "icon volume"
                        )
                }
            }

            LinearProgressIndicator(
                modifier = Modifier.width(100.dp),
                progress = { value / 100f },
                color = Color.White,
                trackColor = Color.Black,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

        }

    }



}

@LandscapePreview
@Composable
fun PlayerAmbientOverlay_Preview() {
    FluxTheme {
        Surface(color = Color.Gray) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerAmbientOverlay(
                    ambientOverlay = { PlayerUiContent.AmbientOverlay(value = 10, type = PlayerUiContent.AmbientOverlay.Type.VOLUME) }
                )
            }

        }
    }
}