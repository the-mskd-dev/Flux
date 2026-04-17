package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
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
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.LandscapePreview
import kotlin.math.roundToInt

@Composable
fun BoxScope.PlayerAmbientOverlay(ambientOverlay: () -> PlayerUiState.AmbientOverlay?) {

    val overlay = ambientOverlay()

    var value by remember { mutableIntStateOf(0) }
    if (overlay?.value != null) {
        value = overlay.value
    }

    AnimatedVisibility(
        modifier = Modifier.align(Alignment.Center),
        visible = overlay != null,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)),
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

            Icon(
                modifier = Modifier.size(size = 36.dp),
                painter = painterResource(R.drawable.ic_volume),
                tint = Color.White,
                contentDescription = "icon volume"
            )

            Text.Label.Large(
                text = value.toString(),
                color = Color.White
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