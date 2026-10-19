package com.mskd.flux.ui.image

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun rememberIconPlaceholderPainter(
    icon: DrawableResource,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f),
    iconTint: Color? = MaterialTheme.colorScheme.primary,
    iconScale: Float = 0.4f,
): Painter {
    val iconPainter = painterResource(icon)
    return remember(iconPainter, backgroundColor, iconTint, iconScale) {
        IconPlaceholderPainter(
            backgroundColor = backgroundColor,
            icon = iconPainter,
            iconTint = iconTint,
            iconScale = iconScale,
        )
    }
}

class IconPlaceholderPainter(
    private val backgroundColor: Color,
    private val icon: Painter,
    private val iconTint: Color? = null,
    private val iconScale: Float = 0.4f,
) : Painter() {

    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        drawRect(backgroundColor)

        val iconSize = size.minDimension * iconScale
        inset(
            horizontal = (size.width - iconSize) / 2f,
            vertical = (size.height - iconSize) / 2f,
        ) {
            with(icon) {
                draw(
                    size = size,
                    colorFilter = iconTint?.let { ColorFilter.tint(it) },
                )
            }
        }
    }
}