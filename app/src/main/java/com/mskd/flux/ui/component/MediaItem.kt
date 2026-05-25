package com.mskd.flux.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.palette.graphics.Palette
import coil3.toBitmap

@Composable
fun MediaItem(
    modifier: Modifier,
    url: String,
    shape: Shape = MaterialTheme.shapes.small,
    onTap: (Int?) -> Unit,
    description: String
) {

    var seedRgb by remember { mutableStateOf<Int?>(null) }

    Image(
        modifier = Modifier
            .clip(shape)
            .then(modifier)
            .clickable { onTap(seedRgb) },
        url = url,
        contentDescription = description,
        onSuccess = { state ->
            val bitmap = state.result.image.toBitmap()
            Palette.from(bitmap).generate { palette ->
                seedRgb = palette?.dominantSwatch?.rgb
            }
        }
    )

}