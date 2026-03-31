package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mskd.flux.screens.player.PlayerUiState
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.utils.LandscapePreview

@Composable
fun BoxScope.PlayerSeekOverlay(seekOverlay: () -> PlayerUiState.SeekOverlay?) {

    val overlay = seekOverlay()

    AnimatedVisibility(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(.33f),
        visible = overlay?.type == PlayerUiState.SeekOverlay.Type.REWIND
    ) {
        PlayerSeekOverlayText(amount = overlay?.amount)
    }

    AnimatedVisibility(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(.33f)
            .align(Alignment.CenterEnd),
        visible = overlay?.type == PlayerUiState.SeekOverlay.Type.FORWARD
    ) {
        PlayerSeekOverlayText(amount = overlay?.amount)
    }

}

@Composable
fun PlayerSeekOverlayText(amount: Int?) {
    AnimatedContent(
        targetState = amount,
        label = "SeekOverlayText"
    ) { amount ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text.Headline.Small(
                text = amount?.toString() ?: "",
                color = Color.White
            )
        }
    }
}

@LandscapePreview
@Composable
fun PlayerSeekOverlay_Preview() {
    AppTheme {
        Surface(color = Color.Black) {
            Box(modifier = Modifier.fillMaxSize()) {
                PlayerSeekOverlay(
                    seekOverlay = { PlayerUiState.SeekOverlay(amount = 10, type = PlayerUiState.SeekOverlay.Type.REWIND) }
                )
                PlayerSeekOverlay(
                    seekOverlay = { PlayerUiState.SeekOverlay(amount = 10, type = PlayerUiState.SeekOverlay.Type.FORWARD) }
                )
            }

        }
    }
}