package com.mskd.flux.features.gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mskd.flux.features.gallery.domain.model.GalleryImage
import com.mskd.flux.features.gallery.domain.model.GalleryImageType
import com.mskd.flux.features.gallery.domain.model.thumbnailUrl
import com.mskd.flux.features.gallery.presentation.GalleryIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.image.Image
import com.mskd.flux.ui.theme.FluxUI

@Composable
fun GalleryScreenContent(
    title: String,
    images: List<GalleryImage>,
    sendIntent: (GalleryIntent) -> Unit,
) {

    val columns = if (rememberScreenDimensions().isLarge) 3 else 2

    FluxScaffold(
        title = title,
        onBackTap = { sendIntent(GalleryIntent.OnBackTap) }
    ) { innerPadding ->

        LazyVerticalStaggeredGrid(
            modifier = Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Fixed(columns),
            contentPadding = PaddingValues(
                start = FluxUI.Space.medium,
                end = FluxUI.Space.medium,
                top = innerPadding.calculateTopPadding() + FluxUI.Space.small,
                bottom = innerPadding.calculateBottomPadding() + FluxUI.Space.medium,
            ),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            verticalItemSpacing = FluxUI.Space.small,
        ) {

            items(items = images, key = { "${it.type}${it.path}" }) { image ->

                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FluxUI.shapes.corners)
                        .aspectRatio(image.ratio.takeIf { it > 0f } ?: 1f),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(image.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Fit,
                    placeholder = Image.placeholder,
                    error = Image.error,
                    contentDescription = null
                )

            }

        }

    }

}

@FluxPreview
@Composable
fun GalleryScreen_Preview() {
    FluxThemePreview {
        GalleryScreenContent(
            title = "Your name",
            images = listOf(
                GalleryImage(path = "/a.jpg", type = GalleryImageType.POSTER, ratio = 2f / 3f, rating = 7.0),
                GalleryImage(path = "/b.jpg", type = GalleryImageType.BACKDROP, ratio = 16f / 9f, rating = 6.0),
                GalleryImage(path = "/c.jpg", type = GalleryImageType.BACKDROP, ratio = 16f / 9f, rating = 5.0),
            ),
            sendIntent = {}
        )
    }
}
