package com.mskd.flux.screens.catalog.composable.viewMode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.catalog.composable.CatalogCategory
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.shows
import org.jetbrains.compose.resources.stringResource

fun LazyGridScope.catalogViewModeGenre(
    artworks: List<Artwork>,
    sortingMode: CatalogSortingMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    item(span = { GridItemSpan(maxLineSpan) }) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
        ) {

            CatalogCategory(
                name = stringResource(Res.string.shows),
                category = ContentType.SHOW,
                artworks = artworks.filter { it.type == ContentType.SHOW && !it.isUnknown },
                sortingOption = sortingMode,
                sendIntent = sendIntent
            )

            CatalogCategory(
                name = stringResource(Res.string.movies),
                category = ContentType.MOVIE,
                artworks = artworks.filter { it.type == ContentType.MOVIE && !it.isUnknown },
                sortingOption = sortingMode,
                sendIntent = sendIntent
            )

        }
    }

}

@Preview
@Composable
fun CatalogViewModeGenre_Preview() {
    FluxThemePreview {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3)
        ) {
            catalogViewModeGenre(
                artworks = MediaMockups.artworks,
                sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                sendIntent = {}
            )
        }
    }
}