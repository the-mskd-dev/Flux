package com.mskd.flux.screens.player.controllers

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

    val fillScale: Float
        get() {
            if (videoSize.width <= 0 || videoSize.height <= 0 || containerSize.width <= 0) return 1f

            val widthRatio = containerSize.width.toFloat() / videoSize.width
            val heightRatio = containerSize.height.toFloat() / videoSize.height

            return maxOf(widthRatio / minOf(widthRatio, heightRatio), heightRatio / minOf(widthRatio, heightRatio))
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