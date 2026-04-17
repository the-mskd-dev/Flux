package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mskd.flux.screens.player.PlayerUiState
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.utils.LandscapePreview

@Composable
fun PlayerAmbientOverlay(ambientOverlay: () -> PlayerUiState.AmbientOverlay?) {

    val overlay = ambientOverlay()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        AnimatedVisibility(
            visible = overlay != null,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
            label = "Visibility ambient overlay"
        ) {

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
                    ambientOverlay = { PlayerUiState.AmbientOverlay(value = 10f, type = PlayerUiState.AmbientOverlay.Type.VOLUME) }
                )
            }

        }
    }
}