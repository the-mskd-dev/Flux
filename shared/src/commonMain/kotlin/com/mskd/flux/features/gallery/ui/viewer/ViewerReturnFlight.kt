package com.mskd.flux.features.gallery.ui.viewer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp

private const val RETURN_FLIGHT_MS = 350

/**
 * The dismissed image flying from where it is on screen back to its thumbnail in the Gallery grid, Google Photos
 * style: its bounds move from [start] to [target] while its corners round up to the [targetCornerRadius] of the
 * thumbnail. Rects and radius are in px, in the coordinates of the viewer.
 */
class ViewerReturnFlight(
    private val start: Rect,
    private val target: Rect,
    private val targetCornerRadius: Float,
) {

    /** From 0 on the screen to 1 on the thumbnail. */
    var progress by mutableFloatStateOf(0f)
        private set

    suspend fun fly() {
        animate(0f, 1f, animationSpec = tween(RETURN_FLIGHT_MS, easing = FastOutSlowInEasing)) { value, _ ->
            progress = value
        }
    }

    /** Maps the image, fitted in a page of [pageSize] with its [ratio], onto the current bounds of the flight. */
    fun GraphicsLayerScope.applyTo(pageSize: Size, ratio: Float) {
        val fitted = fittedImageRect(pageSize, ratio)
        val current = lerp(start, target, progress)
        // Pivot at the center of the page, which is also the center of the fitted image
        scaleX = current.width / fitted.width
        scaleY = current.height / fitted.height
        translationX = current.center.x - fitted.center.x
        translationY = current.center.y - fitted.center.y
        // The clip is drawn before the scale, so the radius is scaled back to land on the thumbnail one
        val radius = lerp(0f, targetCornerRadius, progress)
        val corner = CornerRadius(radius / scaleX, radius / scaleY)
        shape = object : Shape {
            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
                Outline.Rounded(RoundRect(fitted, corner))
        }
        clip = true
    }

}

/**
 * The flight of an image of [ratio] from where the swipe down left it in the viewer, both in root coordinates, to its
 * [thumbnail] rounded like [thumbnailShape]. Null when the thumbnail is not on screen or the ratio is unknown.
 */
fun returnFlightOrNull(
    viewerBounds: Rect,
    ratio: Float,
    dismissState: ViewerDismissState,
    thumbnail: Rect?,
    thumbnailShape: CornerBasedShape,
    density: Density,
): ViewerReturnFlight? {
    if (thumbnail == null || !thumbnail.overlaps(viewerBounds) || ratio <= 0f) return null
    // Where the image is now: fitted in the viewer, then shrunk around its center and moved down by the swipe
    val fitted = fittedImageRect(viewerBounds.size, ratio)
    val startSize = fitted.size * dismissState.scale
    val start = Rect(
        offset = fitted.center + Offset(0f, dismissState.offset) - Offset(startSize.width, startSize.height) / 2f,
        size = startSize,
    )
    val target = thumbnail.translate(-viewerBounds.topLeft)
    return ViewerReturnFlight(
        start = start,
        target = target,
        targetCornerRadius = thumbnailShape.topStart.toPx(target.size, density),
    )
}

/** Bounds of an image of [ratio] (width / height) displayed with ContentScale.Fit, centerd in [container]. */
fun fittedImageRect(container: Size, ratio: Float): Rect {
    val size = if (container.width / container.height > ratio) Size(container.height * ratio, container.height)
    else Size(container.width, container.width / ratio)
    return Rect(Offset((container.width - size.width) / 2f, (container.height - size.height) / 2f), size)
}
