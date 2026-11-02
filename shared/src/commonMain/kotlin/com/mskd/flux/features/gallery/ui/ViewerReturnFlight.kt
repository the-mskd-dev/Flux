package com.mskd.flux.features.gallery.ui

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

    /** Maps the image, fitted in a page of [pageSize] with its [ratio], onto the current bounds of the flight. */
    fun GraphicsLayerScope.applyTo(pageSize: Size, ratio: Float) {
        val fitted = fittedImageRect(pageSize, ratio)
        val current = lerp(start, target, progress)
        // Pivot at the centre of the page, which is also the centre of the fitted image
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

/** Bounds of an image of [ratio] (width / height) displayed with ContentScale.Fit, centred in [container]. */
fun fittedImageRect(container: Size, ratio: Float): Rect {
    val size = if (container.width / container.height > ratio) Size(container.height * ratio, container.height)
    else Size(container.width, container.width / ratio)
    return Rect(Offset((container.width - size.width) / 2f, (container.height - size.height) / 2f), size)
}
