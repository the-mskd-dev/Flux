package com.kaem.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.kaem.flux.screens.player.PlayerIntent
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.extensions.formatMinSec
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSeekBar(
    layoutId: String,
    player: Player,
    showInterface: Boolean,
    isPlaying: Boolean,
    sendIntent: (PlayerIntent) -> Unit
) {

    var sliderPosition by rememberSaveable { mutableFloatStateOf(player.currentPosition.toFloat()) }
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val duration = player.contentDuration

    LaunchedEffect(showInterface, isDragged) {
        while (showInterface && !isDragged) {
            sliderPosition = player.currentPosition.coerceAtLeast(0L).toFloat()
            delay(200)
        }
    }

    Row(
        modifier = Modifier
            .layoutId(layoutId)
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Ui.Space.MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        PlayerSeekBarTime(
            time = { sliderPosition.toLong() }
        )

        PlayerSlider(
            modifier = Modifier.weight(1f),
            value = { sliderPosition },
            onValueChange = {
                sliderPosition = it
                sendIntent(PlayerIntent.UpdateProgress(it.toLong()))
            },
            valueRange = 0f..duration.toFloat(),
            onValueChangeFinished = { sendIntent(PlayerIntent.UpdateProgress(sliderPosition.toLong())) },
            interactionSource = interactionSource,
            isPlaying = isPlaying,
            duration = duration
        )

        PlayerSeekBarTime(
            time = { duration }
        )

    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlayerSlider(
    modifier: Modifier = Modifier,
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit),
    valueRange: ClosedFloatingPointRange<Float>,
    interactionSource : MutableInteractionSource,
    isPlaying: Boolean,
    duration: Long,
) {

    Slider(
        modifier = modifier,
        value = value(),
        onValueChange = onValueChange,
        valueRange = valueRange,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource,
        track = { sliderState ->

            LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                amplitude = { if (isPlaying) 1f else 0f },
                progress = { if (duration > 0) sliderState.value / duration else 0f },
                stopSize = 10.dp
            )

        },
        thumb = {

            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                thumbSize = DpSize(4.dp, 22.dp)
            )
        },
    )

}

@Composable
fun PlayerSeekBarTime(
    time : () -> Long
) {

    Text.Label.Medium(
        text = time().formatMinSec(),
        color = Color.White
    )

}