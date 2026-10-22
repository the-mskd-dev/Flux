package com.mskd.flux.screens.player.composables.playerInterface

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.os.SystemClock
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.player.domain.manager.SeekPrecision
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.formatMinSec
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val FAST_SEEK_INTERVAL_MS = 150L
private const val SETTLE_TOLERANCE_MS = 1_000L
private const val SETTLE_TIMEOUT_MS = 1_000L

@Composable
fun PlayerSeekBar(
    modifier: Modifier,
    isPlaying: Boolean,
    progress: () -> Long,
    duration: Long,
    sendIntent: (PlayerIntent) -> Unit
) {

    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val duration = duration.coerceAtLeast(0L)
    val currentProgress = progress()

    // Finger position while scrubbing (and until the player catches up after release).
    var scrubPosition by remember { mutableStateOf<Float?>(null) }
    var isScrubbing by remember { mutableStateOf(false) }
    var lastFastSeekAt by remember { mutableLongStateOf(0L) }
    val valueRange = remember(duration) { 0f..duration.toFloat() }
    val displayedPosition = scrubPosition ?: currentProgress.toFloat()

    LaunchedEffect(currentProgress, isScrubbing, scrubPosition) {
        val target = scrubPosition
        if (!isScrubbing && target != null && abs(currentProgress - target) <= SETTLE_TOLERANCE_MS) {
            scrubPosition = null
        }
    }

    LaunchedEffect(isScrubbing, scrubPosition) {
        if (!isScrubbing && scrubPosition != null) {
            delay(SETTLE_TIMEOUT_MS)
            scrubPosition = null
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = FluxUI.Space.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        PlayerSeekBarTime(
            time = { displayedPosition.toLong() }
        )

        PlayerSlider(
            modifier = Modifier.weight(1f),
            value = { displayedPosition },
            onValueChange = {
                isScrubbing = true
                scrubPosition = it
                val now = SystemClock.uptimeMillis()
                if (now - lastFastSeekAt >= FAST_SEEK_INTERVAL_MS) {
                    lastFastSeekAt = now
                    sendIntent(PlayerIntent.SeekTo(it.toLong(), SeekPrecision.FAST))
                }
            },
            valueRange = valueRange,
            onValueChangeFinished = {
                isScrubbing = false
                scrubPosition?.let { sendIntent(PlayerIntent.SeekTo(it.toLong(), SeekPrecision.EXACT)) }
            },
            interactionSource = interactionSource,
            isPlaying = isPlaying,
            duration = duration,
        )

        PlayerSeekBarTime(
            time = { duration }
        )

    }

}

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

    val waveProgress = FluxUI.player.waveProgress

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
                amplitude = { if (isPlaying && waveProgress) 1f else 0f },
                progress = { if (duration > 0) sliderState.value / duration else 0f },
                stopSize = 10.dp,
                waveSpeed = 20.dp
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

    Text.Card.Label(
        text = time().formatMinSec(),
        color = Color.White
    )

}