package com.mskd.flux.features.people.ui.components.person

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.mskd.flux.ui.image.FluxImage
import com.mskd.flux.ui.image.rememberIconPlaceholderPainter
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.tmdbImage
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_person

@Composable
fun PersonPicture(
    modifier: Modifier = Modifier,
    name: String,
    profilePath: String?,
    placeholder: Painter = rememberIconPlaceholderPainter(icon = Res.drawable.ic_person),
    onClick: () -> Unit = {}
) {

    Surface(
        modifier = modifier.aspectRatio(FluxUI.Dimension.itemRatio),
        shape = FluxUI.shapes.corners,
        shadowElevation = FluxUI.Elevation.itemShadow,
        onClick = onClick
    ) {

        FluxImage(
            modifier = Modifier.fillMaxSize(),
            path = profilePath?.tmdbImage.orEmpty(),
            placeholder = placeholder,
            error = placeholder,
            contentDescription = name
        )

    }

}