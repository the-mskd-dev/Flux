package com.mskd.flux.screens.catalog.composable.viewMode

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.screens.catalog.composable.CatalogArtworkItem
import com.mskd.flux.ui.theme.FluxUI

fun LazyGridScope.catalogViewModeGrid(
    artworks: List<Artwork>,
    privateFolderEnabled: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    items(items = artworks.filter { !it.isUnknown }, key = { it.id }) { artwork ->

        CatalogArtworkItem(
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .aspectRatio(FluxUI.Dimension.itemRatio),
            artwork = artwork,
            privateFolderEnabled = privateFolderEnabled,
            sendIntent = sendIntent
        )
    }

}