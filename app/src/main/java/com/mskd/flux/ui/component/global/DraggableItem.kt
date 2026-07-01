package com.mskd.flux.ui.component.global

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.mskd.flux.model.core.presentation.SwipeAnchor
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun DraggableItem(
    content: @Composable BoxScope.() -> Unit,
    actionContent: @Composable BoxScope.() -> Unit,
    onActionTap: () -> Unit,
    actionBackgroundColor: Color = MaterialTheme.colorScheme.errorContainer,
    shape: Shape = FluxUI.shapes.corners,
    paddingValues: PaddingValues = PaddingValues(horizontal = FluxUI.Space.medium),
) {

    val density = LocalDensity.current
    val actionWidth = 150.dp
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val anchors = DraggableAnchors {
        SwipeAnchor.OPEN at -actionWidthPx
        SwipeAnchor.CLOSED at 0f
    }

    val bounceSpec = remember {
        spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
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
        animationSpec = bounceSpec
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(paddingValues)
                .padding(2.dp)
                .clip(shape)
                .background(color = actionBackgroundColor)
                .clickable { onActionTap() },
            contentAlignment = Alignment.CenterEnd
        ) {

            actionContent()

        }

        Box(
            modifier = Modifier
                .overscroll(overscrollEffect)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    overscrollEffect = overscrollEffect,
                    flingBehavior = flingBehavior
                )
                .offset { IntOffset(x = state.requireOffset().toInt(), y = 0) }
                .padding(paddingValues)
                .fillMaxWidth()
                .clip(shape),
        ) {

            content()

        }

    }

}