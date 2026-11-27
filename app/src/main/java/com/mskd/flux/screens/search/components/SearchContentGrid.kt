package com.mskd.flux.screens.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
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
import com.mskd.flux.core.model.artwork.shownPosterPath
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.FluxThemePreview
import com.mskd.flux.ui.media.MediaItem

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
        horizontalArrangement = Arrangement.spacedBy(Design.Space.small),
        verticalArrangement = Arrangement.spacedBy(Design.Space.small),
        contentPadding = PaddingValues(bottom = bottomPadding),
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
                        .aspectRatio(Design.Dimension.itemRatio),
                    path = artwork.shownPosterPath,
                    description = artwork.title,
                    onClick = { sendIntent(SearchIntent.OnArtworkTap(artwork = artwork, rgb = it)) }
                )

            }

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
            itemWidth = Design.Dimension.itemWidth,
            sendIntent = {}
        )
    }
}