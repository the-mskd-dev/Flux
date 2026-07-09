package com.mskd.flux.ui.component.global

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.core.SwipeAnchor
import com.mskd.flux.ui.theme.FluxUI
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun DraggableItem(
    content: @Composable BoxScope.() -> Unit,
    actionContent: @Composable BoxScope.() -> Unit,
    onActionTap: () -> Unit,
    modifier: Modifier = Modifier,
    actionBackgroundColor: Color = MaterialTheme.colorScheme.errorContainer,
    shape: Shape = FluxUI.shapes.corners,
    horizontalPadding: Dp = FluxUI.Space.medium
) {
    val density = LocalDensity.current

    val dragWidthPx = remember(density) {
        with(density) { (100.dp + FluxUI.Space.large.times(2)).toPx() }
    }

    val anchors = remember(dragWidthPx) {
        DraggableAnchors {
            SwipeAnchor.OPEN at -dragWidthPx
            SwipeAnchor.CLOSED at 0f
        }
    }

    val state = remember {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.CLOSED,
            anchors = anchors,
        )
    }

    val overscrollEffect = rememberOverscrollEffect()

    val flingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = state,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    var contentHeight by remember { mutableIntStateOf(0) }
    val contentHeightDp = with(density) { contentHeight.toDp() }

    val actionWidthDp by remember {
        derivedStateOf {
            with(density) { state.requireOffset().absoluteValue.toDp() }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {

        Box(
            modifier = Modifier
                .onSizeChanged { contentHeight = it.height }
                .fillMaxWidth()
                .overscroll(overscrollEffect)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    overscrollEffect = overscrollEffect,
                    flingBehavior = flingBehavior
                )
                .offset { IntOffset(x = state.requireOffset().roundToInt(), y = 0) }
                .padding(horizontal = horizontalPadding)
                .clip(shape),
        ) { content() }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = horizontalPadding)
                .height(contentHeightDp)
                .width(actionWidthDp)
                .clip(CircleShape)
                .background(color = actionBackgroundColor)
                .clickable { onActionTap() },
            contentAlignment = Alignment.Center
        ) { actionContent() }
    }
}
