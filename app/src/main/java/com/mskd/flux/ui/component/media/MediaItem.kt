package com.mskd.flux.ui.component.media

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    ratio: Float = FluxUI.Dimension.itemRatio,
    shape: Shape = FluxUI.shapes.corners,
    onClick: (Int?) -> Unit,
    description: String
) {

    var seedRgb by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = modifier
            .aspectRatio(ratio)
            .clickableWithBounce { onClick(seedRgb) },
        shape = shape,
        shadowElevation = FluxUI.Elevation.itemShadow
    ) {

        FluxImage(
            modifier = Modifier.fillMaxSize(),
            path = path,
            contentDescription = description,
            onSuccess = { state ->
                val bitmap = state.result.image.toBitmap()
                Palette.from(bitmap).generate { palette ->
                    seedRgb = palette?.dominantSwatch?.rgb
                }
            }
        )

    }

}