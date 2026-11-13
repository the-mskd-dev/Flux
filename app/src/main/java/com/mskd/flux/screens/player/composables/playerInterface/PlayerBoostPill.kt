package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mskd.flux.screens.player.composables.settings.formatSpeed
import com.mskd.flux.ui.LandscapePreview
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Constants

/** The "2x" pill shown at the top while the Boost speed is active, in the Ambient overlay style. */
@Composable
fun PlayerBoostPill(
    modifier: Modifier = Modifier,
    isBoosting: () -> Boolean
) {

    AnimatedVisibility(
        modifier = modifier,
        visible = isBoosting(),
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        label = "Visibility boost pill"
    ) {

        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = FluxUI.Space.medium)
                .size(size = 48.dp)
                .clip(shape = CircleShape)
                .background(color = Color.Black.copy(alpha = .5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Constants.PLAYER.BOOST_SPEED.formatSpeed(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

    }

}

@LandscapePreview
@Composable
fun PlayerBoostPill_Preview() {
    FluxTheme {
        Surface(color = Color.Gray) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerBoostPill(modifier = Modifier.align(Alignment.TopCenter), isBoosting = { true })
            }
        }
    }
}
