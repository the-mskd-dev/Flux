package com.mskd.flux.ui.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.mskd.flux.ui.expressive.expressivePress

@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    imageVector: ImageVector,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    contentDescription: String
) {

    IconButton(
        modifier = modifier.expressivePress(enabled = enabled),
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(
            imageVector = imageVector,
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
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    contentDescription: String
) {

    IconButton(
        modifier = modifier.expressivePress(enabled = enabled),
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription
        )
    }

}