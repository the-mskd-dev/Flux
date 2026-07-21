package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.itemWidthFor
import com.mskd.flux.utils.rememberScreenDimensions

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogCategory(
    name: String? = null,
    category: ContentType,
    artworks: List<Artwork>,
    sendIntent: (CatalogIntent) -> Unit
) {

    if (artworks.isEmpty())
        return

    val screenDimensions = rememberScreenDimensions()
    val columns = if (screenDimensions.isLarge) 5 else FluxUI.itemsPerRow.artworks
    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }
    val density = LocalDensity.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                with(density) {
                    itemWidth = itemWidthFor(
                        screenWidthDp = size.width.toDp(),
                        columns = columns
                    )
                }
            },
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
    ) {

        Text.Title.Large(
            modifier = Modifier
                .clickable { sendIntent(CatalogIntent.OnCategoryTap(category)) }
                .fillMaxWidth()
                .padding(start = FluxUI.Space.medium, top = FluxUI.Space.large),
            text = name,
            emphasized = true,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            items(artworks, key = { it.id }) {

                MediaItem(
                    modifier = Modifier
                        .width(itemWidth)
                        .aspectRatio(FluxUI.Dimension.itemRatio),
                    path = it.imagePath,
                    onTap = { rgb -> sendIntent(CatalogIntent.OnArtworkTap(artwork = it, rgb = rgb)) },
                    description = it.title
                )

            }

        }

    }
}