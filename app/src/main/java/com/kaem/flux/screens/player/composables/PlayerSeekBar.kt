package com.kaem.flux.screens.player.composables

import android.util.Log
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.media3.exoplayer.ExoPlayer
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSeekBar(
    layoutId: String,
    exoPlayer: ExoPlayer,
    state: PlayerUiState,
    sendIntent: (PlayerIntent) -> Unit
) {

    var sliderPosition by rememberSaveable { mutableFloatStateOf(exoPlayer.currentPosition.toFloat()) }
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(state.showInterface) {
        while (state.showInterface) {
            if (exoPlayer.isPlaying && !isPressed) {
                sliderPosition = exoPlayer.currentPosition.coerceAtLeast(0L).toFloat()
            }
            delay(200)
        }
    }

    Column(
        modifier = Modifier
            .layoutId(layoutId)
            .padding(horizontal = Ui.Space.MEDIUM)
    ) {
        Slider(
            value = sliderPosition,
            valueRange = 0f..exoPlayer.duration.toFloat(),
            interactionSource = interactionSource,
            onValueChange = {
                isPressed = true
                sliderPosition = it
            },
            onValueChangeFinished = {
                isPressed = false
                sendIntent(PlayerIntent.UpdateProgress(sliderPosition.toLong()))
            }
        )
    }

}