package com.kaem.flux.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.kaem.flux.ui.theme.Ui

@Composable
fun MediaItem(
    width: Dp,
    url: String,
    ratio: Float,
    onTap: () -> Unit,
    description: String
) {

    Image(
        modifier = Modifier
            .clickable { onTap() }
            .clip(Ui.Shape.RoundedCorner)
            .width(width)
            .aspectRatio(ratio),
        url = url,
        contentDescription = description
    )

}