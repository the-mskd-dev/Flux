package com.mskd.flux.ui.media

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.image.seedRgb
import com.mskd.flux.ui.image.toComposeImageBitmap
import com.mskd.flux.ui.motion.expressivePress
import kotlinx.coroutines.launch

@Composable
fun MediaItem(
    modifier: Modifier,
    path: String,
    ratio: Float = Design.Dimension.itemRatio,
    shape: Shape = Design.shapes.corners,
    onClick: (Int?) -> Unit,
    onLongClick: (() -> Unit)? = null,
    description: String
) {

    var seedRgb by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .aspectRatio(ratio)
            .expressivePress(
                interactionSource = interactionSource,
                pressedScale = 0.92f
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(seedRgb) },
                onLongClick = onLongClick
            ),
        shape = shape,
        shadowElevation = Design.Elevation.itemShadow
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