package com.mskd.flux.screens.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.genres
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.shows
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchFilters(
    selectedType: ContentType?,
    selectedGenresCount: Int,
    sendIntent: (SearchIntent) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        SearchFilterChip(
            text = stringResource(Res.string.movies),
            selected = selectedType == ContentType.MOVIE,
            onClick = { sendIntent(SearchIntent.FilterOnType(ContentType.MOVIE)) },
        )

        SearchFilterChip(
            text = stringResource(Res.string.shows),
            selected = selectedType == ContentType.SHOW,
            onClick = { sendIntent(SearchIntent.FilterOnType(ContentType.SHOW)) },
        )

        SearchFilterChip(
            text = stringResource(Res.string.genres) + if (selectedGenresCount > 0) " ($selectedGenresCount)" else "",
            selected = selectedGenresCount > 0,
            onClick = { sendIntent(SearchIntent.ShowGenresSelection(show = true)) },
        )

    }

}