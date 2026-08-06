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
import com.mskd.flux.ui.theme.FluxUI

fun LazyGridScope.catalogViewModeGrid(
    artworks: List<Artwork>,
    columns: Int,
    sendIntent: (CatalogIntent) -> Unit
) {

    val startPadding = PaddingValues(start = FluxUI.Space.medium)
    val lastPadding = PaddingValues(end = FluxUI.Space.medium)
    val middlePadding = PaddingValues(horizontal = FluxUI.Space.small)

    itemsIndexed(items = artworks.filter { !it.isUnknown }, key = { _,a -> a.id }) { index, artwork ->

        val columnIndex = index % columns
        val isFirstColumn = columnIndex == 0
        val isLastColumn = columnIndex == columns - 1

        val paddingValues = when {
            isFirstColumn -> startPadding
            isLastColumn -> lastPadding
            else -> middlePadding
        }

        MediaItem(
            modifier = Modifier
                .animateItem()
                .padding(paddingValues)
                .fillMaxWidth(),
            path = artwork.imagePath,
            onTap = { rgb -> sendIntent(CatalogIntent.OnArtworkTap(artwork = artwork, rgb = rgb)) },
            description = artwork.title
        )
    }

}