package com.mskd.flux.screens.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview

@Composable
fun SearchContentGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    artworks: List<Artwork>,
    columns: Int,
    itemWidth: Dp,
    bottomPadding: Dp = 0.dp,
    sendIntent: (SearchIntent) -> Unit
) {

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        state = state
    ) {

        items(
            items = artworks,
            key = { it.id }
        ) { artwork ->

            Box(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                MediaItem(
                    modifier = Modifier
                        .width(itemWidth)
                        .aspectRatio(FluxUI.Dimension.itemRatio),
                    path = artwork.imagePath,
                    description = artwork.title,
                    onClick = { sendIntent(SearchIntent.OnArtworkTap(artwork = artwork, rgb = it)) }
                )

            }

        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(bottomPadding + FluxUI.Space.bottomScreen))
        }

    }

}

@Preview
@Composable
fun SearchContentGrid_Preview() {
    FluxThemePreview {
        SearchContentGrid(
            artworks = MediaMockups.artworks,
            columns = 3,
            itemWidth = FluxUI.Dimension.itemWidth,
            sendIntent = {}
        )
    }
}