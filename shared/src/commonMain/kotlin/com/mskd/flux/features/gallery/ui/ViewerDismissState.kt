package com.mskd.flux.features.gallery.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue

// Share of the viewer height to drag past for the release to dismiss
private const val DISMISS_THRESHOLD = 0.25f
private const val MIN_SCALE = 0.85f

enum class ViewerDismissRelease { DISMISS, SETTLE }

/**
 * Swipe down to dismiss the Gallery viewer, like a bottom sheet: the image follows the finger below its resting
 * position, shrinks and fades the background with the distance, and the release decides whether the viewer closes.
 * [flingVelocity] is the downward velocity, in px/s, from which a release dismisses whatever the distance.
 */
class ViewerDismissState(private val flingVelocity: Float) {

    /** Height of the viewer, in px. */
    var height by mutableFloatStateOf(0f)

    /** Vertical offset of the image from its resting position, in px. */
    var offset by mutableFloatStateOf(0f)
        private set

    // 0 at rest, 1 from the dismiss threshold on
    private val progress: Float
        get() = if (height <= 0f) 0f else (offset / (height * DISMISS_THRESHOLD)).coerceIn(0f, 1f)

    val scale: Float get() = 1f - (1f - MIN_SCALE) * progress

    val backgroundAlpha: Float get() = 1f - progress

    fun drag(delta: Float) {
        offset = (offset + delta).coerceAtLeast(0f)
    }

    /** Moves the image to [offset], used by the settle back and dismiss animations. */
    fun snapTo(offset: Float) {
        this.offset = offset.coerceAtLeast(0f)
    }

    /** Decides what the release of the drag does, from the reached offset and the vertical [velocity] in px/s. */
    fun release(velocity: Float): ViewerDismissRelease {
        val pastThreshold = progress >= 1f && velocity > -flingVelocity
        return if (pastThreshold || velocity >= flingVelocity) ViewerDismissRelease.DISMISS
        else ViewerDismissRelease.SETTLE
    }

}
