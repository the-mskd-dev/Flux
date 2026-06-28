package com.mskd.flux.ui.component.media

import androidx.compose.foundation.layout.aspectRatio
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
import com.mskd.flux.ui.component.global.FluxImage
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.clickableWithBounce

@Composable
fun MediaItem(
    modifier: Modifier,
    path: String,
    hd: Boolean,
    ratio: Float = FluxUI.Dimension.itemRatio,
    shape: Shape = FluxUI.shapes.corners,
    onTap: (Int?) -> Unit,
    description: String
) {

    var seedRgb by remember { mutableStateOf<Int?>(null) }

    FluxImage(
        modifier = Modifier
            .clip(shape)
            .then(modifier)
            .aspectRatio(ratio)
            .clickableWithBounce { onTap(seedRgb) },
        path = path,
        hd = hd,
        contentDescription = description,
        onSuccess = { state ->
            val bitmap = state.result.image.toBitmap()
            Palette.from(bitmap).generate { palette ->
                seedRgb = palette?.dominantSwatch?.rgb
            }
        }
    )

}