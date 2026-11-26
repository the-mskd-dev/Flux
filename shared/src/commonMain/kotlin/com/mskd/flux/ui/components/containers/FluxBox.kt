package com.mskd.flux.ui.components.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.LocalUiShapes

@Composable
fun FluxBox(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
    content: @Composable BoxScope.() -> Unit
) {

    Box(
        modifier = modifier
            .clip(LocalUiShapes.current.corners)
            .background(background)
            .padding(
                vertical = Design.Space.small,
                horizontal = Design.Space.medium
            ),
        content = content
    )
}