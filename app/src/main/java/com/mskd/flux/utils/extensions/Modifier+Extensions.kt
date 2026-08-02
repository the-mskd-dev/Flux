package com.mskd.flux.utils.extensions

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.theme.FluxUI

fun Modifier.grayScale() : Modifier {
    val saturationMatrix = ColorMatrix().apply { setToSaturation(0f) }
    val saturationFilter = ColorFilter.colorMatrix(saturationMatrix)
    val paint = Paint().apply { colorFilter = saturationFilter }

    return drawWithCache {
        val canvasBounds = Rect(Offset.Zero, size)
        onDrawWithContent {
            drawIntoCanvas {
                it.saveLayer(canvasBounds, paint)
                drawContent()
                it.restore()
            }
        }
    }
}

@Composable
fun Modifier.clickableWithBounce(
    onClick: () -> Unit
): Modifier {

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) .92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )

    return this
        .graphicsLayer { scaleX = animatedScale; scaleY = animatedScale }
        .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@Composable
fun Modifier.groupedShape(
    index: Int,
    lastIndex: Int,
    cornerRadius: Dp = FluxUI.shapes.listItem
): Modifier {
    val shape = when {
        lastIndex == 0 -> RoundedCornerShape(size = cornerRadius)
        index == 0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius, bottomStart = 4.dp, bottomEnd = 4.dp)
        index == lastIndex -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = cornerRadius, bottomEnd = cornerRadius)
        else -> RoundedCornerShape(4.dp)
    }
    return this.clip(shape)
}

@Composable
fun Modifier.fillMaxWidthWithLimit() : Modifier{
    return this
        .widthIn(max = 600.dp)
        .fillMaxWidth()
}