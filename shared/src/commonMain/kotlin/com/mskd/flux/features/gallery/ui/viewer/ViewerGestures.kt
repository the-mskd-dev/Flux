package com.mskd.flux.features.gallery.ui.viewer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import kotlinx.coroutines.CancellationException
import kotlin.math.abs

/**
 * Pinch and zoomed pan of [zoom], and the swipe down to dismiss on [dismissState] (null when the swipe down is not
 * available). Only consumes what it handles, so a horizontal 1x drag still reaches the pager: past the touch slop,
 * a 1x drag mainly downward becomes the swipe down, any other one is left alone.
 */
suspend fun PointerInputScope.detectZoomAndSwipeDown(
    zoom: ZoomState,
    dismissState: () -> ViewerDismissState?,
    onSwipeDownStart: () -> Unit,
    onSwipeDownEnd: (velocity: Float) -> Unit,
    onSwipeDownCancel: () -> Unit,
) = awaitEachGesture {
    val down = awaitFirstDown(requireUnconsumed = false)
    val velocityTracker = VelocityTracker()
    var drag = Offset.Zero
    var gestureClaimed = false
    var swipingDown = false
    try {
        do {
            val event = awaitPointerEvent()
            val multiTouch = event.changes.count { it.pressed } > 1
            val change = event.changes.firstOrNull { it.id == down.id }
            if (swipingDown) {
                if (change != null) {
                    velocityTracker.addPointerInputChange(change)
                    dismissState()?.drag(change.positionChange().y)
                    change.consume()
                }
            } else if (multiTouch || zoom.isZoomed) {
                gestureClaimed = true
                val zoomChange = event.calculateZoom()
                val pan = event.calculatePan()
                if (zoomChange != 1f || pan != Offset.Zero) {
                    zoom.transform(zoomChange, event.calculateCentroid(useCurrent = false), pan)
                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                }
            } else if (!gestureClaimed && change != null) {
                val state = dismissState() ?: continue
                velocityTracker.addPointerInputChange(change)
                drag += change.positionChange()
                if (drag.getDistance() > viewConfiguration.touchSlop) {
                    gestureClaimed = true
                    if (drag.y > abs(drag.x)) {
                        swipingDown = true
                        onSwipeDownStart()
                        state.drag(change.positionChange().y)
                        change.consume()
                    }
                }
            }
        } while (event.changes.any { it.pressed })
        if (swipingDown) onSwipeDownEnd(velocityTracker.calculateVelocity().y)
    } catch (e: CancellationException) {
        // A page leaving mid-swipe settles the image back rather than leaving it off its resting position
        if (swipingDown) onSwipeDownCancel()
        throw e
    }
}
