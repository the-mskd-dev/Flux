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
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.screens.player.PlayerUiState
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.LandscapePreview

@Composable
fun PlayerAmbientOverlay(
    modifier: Modifier = Modifier,
    ambientOverlay: () -> PlayerUiState.AmbientOverlay?) {

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
                .clip(shape = Ui.Shape.Corner.Medium)
                .background(color = Color.Black.copy(alpha = .5f))
                .padding(vertical = Ui.Space.MEDIUM, horizontal = Ui.Space.LARGE),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Crossfade(
                targetState = overlay?.type,
                label = "Crossfade ambient overlay"
            ) {
                when (it) {
                    PlayerUiState.AmbientOverlay.Type.BRIGHTNESS ->
                        Icon(
                            modifier = Modifier.size(size = 36.dp),
                            painter = painterResource(R.drawable.ic_brightness),
                            tint = Color.White,
                            contentDescription = "icon brightness"
                        )
                    else ->
                        Icon(
                            modifier = Modifier.size(size = 36.dp),
                            painter = painterResource(R.drawable.ic_volume),
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
    AppTheme {
        Surface(color = Color.Gray) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerAmbientOverlay(
                    ambientOverlay = { PlayerUiState.AmbientOverlay(value = 10, type = PlayerUiState.AmbientOverlay.Type.VOLUME) }
                )
            }

        }
    }
}