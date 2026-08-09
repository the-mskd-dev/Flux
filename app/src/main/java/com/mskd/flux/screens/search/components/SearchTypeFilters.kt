package com.mskd.flux.screens.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.shows
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTypeFilters(
    selectedType: ContentType?,
    sendIntent: (SearchIntent) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        SearchTypeFilterChip(
            text = stringResource(Res.string.movies),
            selected = selectedType == ContentType.MOVIE,
            onClick = { sendIntent(SearchIntent.FilterOnType(ContentType.MOVIE)) },
        )

        SearchTypeFilterChip(
            text = stringResource(Res.string.shows),
            selected = selectedType == ContentType.SHOW,
            onClick = { sendIntent(SearchIntent.FilterOnType(ContentType.SHOW)) },
        )

    }
}

@Composable
fun SearchTypeFilterChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {

    FilterChip(
        onClick = onClick,
        label = {
            Text.Button.Chip(
                text = text.uppercase(),
            )
        },
        selected = selected,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "$text selected",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else { null },
    )

}