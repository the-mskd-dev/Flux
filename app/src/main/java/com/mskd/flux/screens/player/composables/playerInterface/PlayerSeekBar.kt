package com.mskd.flux.screens.player.composables.playerInterface

import android.os.SystemClock
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mskd.flux.features.player.domain.manager.SeekPrecision
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.presentation.PlayerIntent
import com.mskd.flux.features.player.ui.ChapterSlider
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.LocalUiPlayer
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
    enabled: Boolean,
    showWave: Boolean,
    chapters: List<VideoChapter>,
    sendIntent: (PlayerIntent) -> Unit
) {

    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val duration = duration.coerceAtLeast(0L)
    val currentProgress = progress()

    // Finger position while scrubbing (and until the player catches up after release).
    var scrubPosition by remember { mutableStateOf<Float?>(null) }
    var isScrubbing by remember { mutableStateOf(false) }
    var lastFastSeekAt by remember { mutableLongStateOf(0L) }
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
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .padding(horizontal = Design.Space.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Design.Space.small)
    ) {

        PlayerSeekBarTime(
            time = { displayedPosition.toLong() }
        )

        ChapterSlider(
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
            onValueChangeFinished = {
                isScrubbing = false
                scrubPosition?.let { sendIntent(PlayerIntent.SeekTo(it.toLong(), SeekPrecision.EXACT)) }
            },
            interactionSource = interactionSource,
            isPlaying = isPlaying,
            duration = duration,
            chapters = chapters,
            isScrubbing = isScrubbing,
            enabled = enabled,
            showWave = showWave,
            waveAnimated = LocalUiPlayer.current.waveProgress,
        )

        PlayerSeekBarTime(
            time = { duration }
        )

    }

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