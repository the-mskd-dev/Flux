package com.mskd.flux.screens.catalog.composable.viewMode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.catalog.composable.CatalogCategory
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.bleedHorizontal

fun LazyGridScope.catalogViewModeGenre(
    artworks: List<Artwork>,
    genres: List<Genre>,
    sortingMode: CatalogSortingMode,
    sendIntent: (CatalogIntent) -> Unit
) {

    item(span = { GridItemSpan(maxLineSpan) }, key = "catalog_genre_content") {

        Column(
            modifier = Modifier
                .animateItem()
                .bleedHorizontal(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.large)
        ) {

            val categories = remember(artworks, genres) {
            val validArtworks = artworks.filterNot { it.isUnknown }

                genres
                    .map { genre -> genre to validArtworks.filter { it.genreIds.contains(genre.id) } }
                    .sortedByDescending { (_, genreArtworks) -> genreArtworks.size }
            }

            categories.forEach { (genre, categoryArtworks) ->
                CatalogCategory(
                    name = genre.name,
                    artworks = categoryArtworks,
                    sortingOption = sortingMode,
                    onCategoryTap = { sendIntent(CatalogIntent.OnGenreTap(genre = genre)) },
                    sendIntent = sendIntent
                )
            }

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
                genres = DetailsMockup.allGenres,
                sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                sendIntent = {}
            )
        }
    }
}