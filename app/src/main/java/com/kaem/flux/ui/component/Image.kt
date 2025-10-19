package com.kaem.flux.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun Image(
    modifier: Modifier,
    url: String,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String
) {
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentScale = contentScale,
        placeholder = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f)),
        error = ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f)),
        contentDescription = contentDescription
    )

}