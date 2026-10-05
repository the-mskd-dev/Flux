package com.mskd.flux.presentation.media

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.combinedClickableWithBounce
import com.mskd.flux.presentation.image.FluxImage
import com.mskd.flux.presentation.image.seedRgb
import com.mskd.flux.presentation.image.toComposeImageBitmap
import kotlinx.coroutines.launch

@Composable
fun MediaItem(
    modifier: Modifier,
    path: String,
    ratio: Float = FluxUI.Dimension.itemRatio,
    shape: Shape = FluxUI.shapes.corners,
    onClick: (Int?) -> Unit,
    onLongClick: (() -> Unit)? = null,
    description: String
) {

    var seedRgb by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier
            .aspectRatio(ratio)
            .combinedClickableWithBounce(
                onClick = { onClick(seedRgb) },
                onLongClick = onLongClick
            ),
        shape = shape,
        shadowElevation = FluxUI.Elevation.itemShadow
    ) {

        FluxImage(
            modifier = Modifier.fillMaxSize(),
            path = path,
            contentDescription = description,
            onSuccess = { state ->
                val image = state.result.image
                scope.launch { seedRgb = image.toComposeImageBitmap().seedRgb() }
            }
        )

    }

}