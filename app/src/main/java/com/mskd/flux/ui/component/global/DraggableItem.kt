package com.mskd.flux.ui.component.global

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mskd.flux.model.core.presentation.SwipeAnchor
import com.mskd.flux.ui.theme.FluxUI

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
    val actionWidth = 24.dp + FluxUI.Space.large.times(2)
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val anchors = DraggableAnchors {
        SwipeAnchor.OPEN at -actionWidthPx
        SwipeAnchor.CLOSED at 0f
    }
    val state = remember {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.CLOSED,
            anchors = anchors
        )
    }

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
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal
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