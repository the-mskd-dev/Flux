package com.mskd.flux.screens.catalog.composable.viewMode

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.utils.extensions.gridItemPadding

fun LazyGridScope.catalogViewModeGrid(
    artworks: List<Artwork>,
    columns: Int,
    sendIntent: (CatalogIntent) -> Unit
) {

    itemsIndexed(items = artworks.filter { !it.isUnknown }, key = { _,a -> a.id }) { index, artwork ->

        MediaItem(
            modifier = Modifier
                .animateItem()
                .gridItemPadding(index = index, columns = columns)
                .fillMaxWidth(),
            path = artwork.imagePath,
            onTap = { rgb -> sendIntent(CatalogIntent.OnArtworkTap(artwork = artwork, rgb = rgb)) },
            description = artwork.title
        )
    }

}