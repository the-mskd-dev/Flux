package com.kaem.flux.screens.player.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.kaem.flux.model.artwork.Media
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.screens.player.PlayerUiState
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSeekBar(
    layoutId: String,
    exoPlayer: ExoPlayer,
    state: PlayerUiState,
    sendIntent: (PlayerIntent) -> Unit
) {

    var sliderPosition by rememberSaveable { mutableFloatStateOf(exoPlayer.currentPosition.toFloat()) }
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()

    LaunchedEffect(state.showInterface, isDragged) {
        while (state.showInterface && !isDragged) {
            sliderPosition = exoPlayer.currentPosition.coerceAtLeast(0L).toFloat()
            delay(200)
        }
    }

    Slider(
        modifier = Modifier
            .layoutId(layoutId)
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.MEDIUM),
        value = sliderPosition,
        valueRange = 0f..exoPlayer.duration.toFloat(),
        interactionSource = interactionSource,
        onValueChange = { sliderPosition = it },
        onValueChangeFinished = { sendIntent(PlayerIntent.UpdateProgress(sliderPosition.toLong())) },
        track = { sliderState ->

            LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                amplitude = { if (state.isPlaying && it in 0.1f..0.95f) 1f else 0f },
                progress = { if (exoPlayer.duration > 0) sliderState.value / exoPlayer.duration else 0f },
                stopSize = 10.dp
            )

        },
        thumb = {

            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                thumbSize = DpSize(4.dp, 22.dp)
            )
        }
    )

}