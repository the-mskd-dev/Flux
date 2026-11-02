package com.mskd.flux.features.gallery.ui.viewer

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 1f
private const val DOUBLE_TAP_ZOOM = 3f
private const val MAX_ZOOM = 10f
private const val ZOOM_ANIMATION_MS = 250

/**
 * Zoom of a Gallery viewer page, from 1x to 10x: pinch, pan once zoomed and double-tap.
 * The zoomed image never leaves the page. Focus points are relative to the top left corner of the page.
 */
@Stable
class ZoomState(private val scope: CoroutineScope) {

    var scale by mutableFloatStateOf(MIN_ZOOM)
        private set

    /** Translation of the image from the centre of the page, in px. */
    var offset by mutableStateOf(Offset.Zero)
        private set

    /** Size of the page, in px. */
    var pageSize by mutableStateOf(IntSize.Zero)

    val isZoomed: Boolean get() = scale > MIN_ZOOM

    private var animation: Job? = null

    private val center: Offset get() = Offset(pageSize.width / 2f, pageSize.height / 2f)

    /** Zooms by [zoom] keeping the content under [focus] fixed on screen, then pans by [pan]. */
    fun transform(zoom: Float, focus: Offset, pan: Offset) {
        animation?.cancel()
        val target = (scale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
        offset = clamp(zoomedOffset(target, focus - center) + pan, target)
        scale = target
    }

    /** Zooms in on [focus] at 1x, back to 1x otherwise. */
    fun toggleZoom(focus: Offset) {
        animateZoomTo(if (isZoomed) MIN_ZOOM else DOUBLE_TAP_ZOOM, focus - center)
    }

    fun animateZoomOut() {
        animateZoomTo(MIN_ZOOM, Offset.Zero)
    }

    fun reset() {
        animation?.cancel()
        scale = MIN_ZOOM
        offset = Offset.Zero
    }

    // Focus relative to the centre of the page
    private fun animateZoomTo(target: Float, centredFocus: Offset) {
        animation?.cancel()
        val startScale = scale
        val startOffset = offset
        val endOffset = clamp(zoomedOffset(target, centredFocus), target)
        animation = scope.launch {
            animate(0f, 1f, animationSpec = tween(ZOOM_ANIMATION_MS)) { progress, _ ->
                scale = startScale + (target - startScale) * progress
                offset = startOffset + (endOffset - startOffset) * progress
            }
        }
    }

    // The offset that keeps the content under the centred focus fixed when zooming to target
    private fun zoomedOffset(target: Float, centredFocus: Offset): Offset =
        centredFocus - (centredFocus - offset) * (target / scale)

    // Keeps the zoomed image from being dragged out of the page
    private fun clamp(offset: Offset, scale: Float): Offset {
        val maxX = pageSize.width * (scale - 1f) / 2f
        val maxY = pageSize.height * (scale - 1f) / 2f
        return Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
    }

}

@Composable
fun rememberZoomState(): ZoomState {
    val scope = rememberCoroutineScope()
    return remember(scope) { ZoomState(scope) }
}
