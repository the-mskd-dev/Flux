package com.mskd.flux.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.motion.expressivePress

@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String
) {

    Box(
        modifier = modifier
            .expressivePress(enabled = enabled)
            .clip(CircleShape)
            .size(IconButtonDefaults.smallContainerSize())
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(containerColor)
        )

        Icon(
            imageVector = imageVector,
            tint = contentColor,
            contentDescription = contentDescription
        )

    }

}

@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    painter: Painter,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String
) {

    Box(
        modifier = modifier
            .expressivePress(enabled = enabled)
            .clip(CircleShape)
            .size(IconButtonDefaults.smallContainerSize())
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(containerColor)
        )

        Icon(
            painter = painter,
            tint = contentColor,
            contentDescription = contentDescription
        )

    }

}

@Preview
@Composable
fun SmallButton_Preview() {
    FluxThemePreview {
        SmallButton(
            onClick = {},
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "preview button"
        )
    }
}