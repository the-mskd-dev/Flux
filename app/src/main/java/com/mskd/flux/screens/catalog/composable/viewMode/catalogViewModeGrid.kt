package com.mskd.flux.screens.catalog.composable.viewMode

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.media.MediaItem

fun LazyGridScope.catalogViewModeGrid(
    artworks: List<Artwork>,
    sendIntent: (CatalogIntent) -> Unit
) {

    items(items = artworks.filter { !it.isUnknown }, key = { it.id }) { artwork ->

        MediaItem(
            modifier = Modifier
                .animateItem().fillMaxWidth(),
            path = artwork.imagePath,
            onClick = { rgb -> sendIntent(CatalogIntent.OnArtworkTap(artwork = artwork, rgb = rgb)) },
            description = artwork.title
        )
    }

}