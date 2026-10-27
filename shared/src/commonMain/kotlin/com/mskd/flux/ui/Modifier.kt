package com.mskd.flux.ui

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.theme.FluxUI
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurDefaults
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.material3.Material3
import dev.chrisbanes.haze.hazeSource

@Composable
fun Modifier.blurBackground(
    state: HazeState,
    zIndex: Float = 0f,
    key: Any? = null
) = this.hazeSource(
    state = state,
    zIndex = zIndex,
    key = key
)

@Composable
fun Modifier.blurForeground(
    state: HazeState,
    alpha: Float = 1f,
    radius: Dp = HazeBlurDefaults.blurRadius,
    performanceMode: HazePerformanceMode? = null,
    expandLayerBounds: Boolean = true,
    progressive: Boolean = false
) = this.hazeBlur(
    input = HazeInput.Sources(state),
    style = HazeBlurStyle.Material3 {
        alpha(alpha)
        blurRadius(radius)
        if (progressive) {
            progressive(
                HazeProgressive.verticalGradient(
                    startIntensity = 1f,
                    endIntensity = 0f,
                ),
            )
        }
    },
    performanceMode = performanceMode,
    expandLayerBounds = expandLayerBounds
)

/**
 * Blurs the content of the composable itself (no source/effect pair). The blur is forced on so that
 * Haze falls back to its RenderScript delegate on API 29-30, and its radius is animated.
 */
@Composable
fun Modifier.blurContent(
    blurred: Boolean,
    radius: Dp = 4.dp,
): Modifier {
    val animatedRadius by animateDpAsState(
        targetValue = if (blurred) radius else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blur_content_radius"
    )
    return if (animatedRadius > 0.dp) {
        this.hazeBlur(
            input = HazeInput.Content,
            style = HazeBlurStyle {
                blurEnabled(true)
                blurRadius(animatedRadius)
            },
            expandLayerBounds = false
        )
    } else this
}

/**
 * Hides text. On API 31+ it uses Compose's native blur, which fades out past the bounds without
 * tearing; below, where that blur is a no-op, the text is cross-faded with redaction bars.
 */
@Composable
fun Modifier.blurText(
    blurred: Boolean,
    radius: Dp = 8.dp,
): Modifier {
    val progress by animateFloatAsState(
        targetValue = if (blurred) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "blur_text_progress"
    )

    if (supportsNativeBlur) {
        return if (progress > 0f) this.blur(radius * progress, BlurredEdgeTreatment.Unbounded) else this
    }

    val barColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .2f)
    return this.drawWithContent {
        if (progress < 1f) {
            if (progress > 0f) {
                drawIntoCanvas {
                    it.saveLayer(Rect(Offset.Zero, size), Paint().apply { alpha = 1f - progress })
                    drawContent()
                    it.restore()
                }
            } else drawContent()
        }
        if (progress > 0f) {
            val barHeight = 12.dp.toPx()
            val spacing = 6.dp.toPx()
            val lines = ((size.height + spacing) / (barHeight + spacing)).toInt().coerceAtLeast(1)
            repeat(lines) { line ->
                val width = if (line == lines - 1 && lines > 1) size.width * .6f else size.width
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(0f, line * (barHeight + spacing)),
                    size = Size(width, barHeight),
                    cornerRadius = CornerRadius(barHeight / 2),
                    alpha = progress
                )
            }
        }
    }
}

@Composable
fun Modifier.fillMaxWidthWithLimit(
    max: Dp = 600.dp,
    @FloatRange fraction: Float = 1f
) : Modifier {
    return this
        .widthIn(max = max)
        .fillMaxWidth(fraction = fraction)
}

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.combinedClickableWithBounce(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
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
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick
        )
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

fun Modifier.bleedHorizontal(amount: Dp = FluxUI.Space.medium) = layout { measurable, constraints ->
    val expandPx = amount.roundToPx()

    val expandedConstraints = constraints.copy(
        minWidth = (constraints.minWidth + expandPx * 2).coerceAtLeast(0),
        maxWidth = constraints.maxWidth + expandPx * 2
    )

    val placeable = measurable.measure(expandedConstraints)

    layout(placeable.width - expandPx * 2, placeable.height) {
        placeable.placeRelative(x = -expandPx, y = 0)
    }
}

@Composable
fun Modifier.displayCutoutPaddingInLandscape() : Modifier {
    val screenDimensions = rememberScreenDimensions()

    return this.then(
        if (screenDimensions.isLarge) Modifier.displayCutoutPadding() else Modifier
    )
}