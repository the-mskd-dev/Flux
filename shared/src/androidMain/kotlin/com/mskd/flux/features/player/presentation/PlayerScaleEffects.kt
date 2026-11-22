package com.mskd.flux.features.player.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.media3.common.VideoSize

class PlayerScaleEffects(
    private val videoSize: VideoSize,
    private val containerSize: IntSize,
    private val isPortrait: Boolean,
) {
    var isFullPage by mutableStateOf(false)
        private set

    /** Scale that makes the fitted picture cover the whole container. */
    val fillScale: Float
        get() {
            val videoRatio = videoSize.displayAspectRatio ?: return 1f
            if (containerSize.width <= 0 || containerSize.height <= 0) return 1f

            val containerRatio = containerSize.width.toFloat() / containerSize.height
            return maxOf(videoRatio / containerRatio, containerRatio / videoRatio)
        }

    val targetScale: Float get() = if (isFullPage) fillScale else 1f

    fun toggleFill(fill: Boolean) {
        if (!isPortrait) {
            isFullPage = fill
        }
    }
}

@Composable
fun rememberPlayerScaleEffects(
    videoSize: VideoSize,
    containerSize: IntSize,
    isPortrait: Boolean
): PlayerScaleEffects {
    return remember(videoSize, containerSize, isPortrait) {
        PlayerScaleEffects(
            videoSize = videoSize,
            containerSize = containerSize,
            isPortrait = isPortrait
        )
    }
}