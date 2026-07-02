package com.mskd.flux.ui.component.global

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Row
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.mskd.flux.model.core.presentation.SwipeAnchor
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.Trace
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun DraggableItem1(
    content: @Composable BoxScope.() -> Unit,
    actionContent: @Composable BoxScope.() -> Unit,
    onActionTap: () -> Unit,
    actionBackgroundColor: Color = MaterialTheme.colorScheme.errorContainer,
    shape: Shape = FluxUI.shapes.corners,
    horizontalPadding: Dp = FluxUI.Space.medium
) {

    val density = LocalDensity.current
    val actionWidth = 100.dp + FluxUI.Space.large.times(2)
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val anchors = DraggableAnchors {
        SwipeAnchor.OPEN at -actionWidthPx
        SwipeAnchor.CLOSED at 0f
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

    val actionIconWidth by remember {
        derivedStateOf {
            with(density) { state.requireOffset().absoluteValue.toDp() }
        }
    }

    val animatedWidth by animateDpAsState(
        targetValue = actionIconWidth,
        label = "ActionWidth"
    )

    ConstraintLayout(
        modifier = Modifier.fillMaxWidth(),
    ) {

        val (content, action) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(action) {
                    top.linkTo(content.top)
                    bottom.linkTo(content.bottom)
                    end.linkTo(parent.end, horizontalPadding)
                    width = Dimension.value(animatedWidth)
                    height = Dimension.percent(1f)
                }
                .clip(CircleShape)
                .background(color = actionBackgroundColor)
                .clickable { onActionTap() },
            contentAlignment = Alignment.Center
        ) {

            actionContent()

        }

        Box(
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.matchParent
                    height = Dimension.wrapContent
                }
                .overscroll(overscrollEffect)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    overscrollEffect = overscrollEffect,
                    flingBehavior = flingBehavior
                )
                .offset { IntOffset(x = state.requireOffset().toInt(), y = 0) }
                .padding(horizontal = horizontalPadding)
                .clip(shape),
        ) {

            content()

        }

    }

}

@Composable
fun DraggableItem2(
    content: @Composable BoxScope.() -> Unit,
    actionContent: @Composable BoxScope.() -> Unit,
    onActionTap: () -> Unit,
    actionBackgroundColor: Color = MaterialTheme.colorScheme.errorContainer,
    shape: Shape = FluxUI.shapes.corners,
    horizontalPadding: Dp = FluxUI.Space.medium
) {

    val density = LocalDensity.current
    val dragWidth = with(density) { (100.dp + FluxUI.Space.large.times(2)).toPx() }
    val anchors = DraggableAnchors {
        SwipeAnchor.OPEN at -dragWidth
        SwipeAnchor.CLOSED at 0f
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

    var actionHeight by remember { mutableIntStateOf(0) }
    val actionHeightDp by remember {
        derivedStateOf {
            with(density) { actionHeight.toDp() }
        }
    }
    val actionWidth by remember {
        derivedStateOf {
            with(density) { state.requireOffset().absoluteValue.toDp() }
        }
    }

    val animatedActionWidth by animateDpAsState(
        targetValue = actionWidth,
        label = "ActionWidth"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = horizontalPadding)
                .height(actionHeightDp)
                .width(animatedActionWidth)
                .clip(CircleShape)
                .background(color = actionBackgroundColor)
                .clickable {
                    Trace.debug(message = "Click action")
                    //onActionTap()
               },
            contentAlignment = Alignment.Center
        ) {

            actionContent()

        }

        Box(
            modifier = Modifier
                .onSizeChanged { actionHeight = it.height }
                .fillMaxWidth()
                .overscroll(overscrollEffect)
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    overscrollEffect = overscrollEffect,
                    flingBehavior = flingBehavior
                )
                .offset { IntOffset(x = state.requireOffset().toInt(), y = 0) }
                .padding(horizontal = horizontalPadding)
                .clip(shape)
                .clickable {
                    Trace.debug(message = "Click content")
                    //onActionTap()
                },
        ) {

            content()

        }

    }


}
