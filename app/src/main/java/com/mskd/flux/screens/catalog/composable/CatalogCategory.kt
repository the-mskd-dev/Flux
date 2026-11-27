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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.Design
import com.mskd.flux.ui.dimensions.itemWidthFor
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.text.Text

@Composable
fun CatalogCategory(
    name: String? = null,
    artworks: List<Artwork>,
    sortingOption: CatalogSortingMode,
    privateFolderEnabled: Boolean,
    startPadding: Dp = Design.Space.medium,
    endPadding: Dp = Design.Space.medium,
    onCategoryTap: () -> Unit,
    sendIntent: (CatalogIntent) -> Unit,
) {

    if (artworks.isEmpty())
        return

    val listState = rememberLazyListState()
    val screenDimensions = rememberScreenDimensions()
    val columns = if (screenDimensions.isLarge) 5 else Design.itemsPerRow.artworks
    var itemWidth by remember { mutableStateOf(Design.Dimension.itemWidth) }
    val density = LocalDensity.current

    LaunchedEffect(sortingOption) {
        listState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                with(density) {
                    val usableWidth = (size.width.toDp() - (startPadding - Design.Space.medium) - (endPadding - Design.Space.medium)).coerceAtLeast(0.dp)
                    itemWidth = itemWidthFor(
                        screenWidthDp = usableWidth,
                        columns = columns
                    )
                }
            },
        verticalArrangement = Arrangement.spacedBy(Design.Space.medium)
    ) {

        Text.Content.Title(
            modifier = Modifier
                .clickable { onCategoryTap() }
                .fillMaxWidth()
                .padding(start = startPadding, end = endPadding),
            text = name,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(start = startPadding, end = endPadding),
            horizontalArrangement = Arrangement.spacedBy(Design.Space.small)
        ) {

            items(artworks, key = { it.id }) {

                CatalogArtworkItem(
                    modifier = Modifier
                        .animateItem()
                        .width(itemWidth)
                        .aspectRatio(Design.Dimension.itemRatio),
                    artwork = it,
                    privateFolderEnabled = privateFolderEnabled,
                    sendIntent = sendIntent
                )

            }

        }

    }
}