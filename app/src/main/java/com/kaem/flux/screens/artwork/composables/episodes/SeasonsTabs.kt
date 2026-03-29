package com.kaem.flux.screens.artwork.composables.episodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaem.flux.R
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.FluxPreview

@Composable
fun SeasonsTabs(
    selectedSeason: Int,
    seasons: List<Int>,
    onSeasonTap: (Int) -> Unit
) {

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        items(items = seasons.sorted(), key = { it }) { season ->

            val isSelected = selectedSeason == season

            FilterChip(
                onClick = { onSeasonTap(season) },
                label = {
                    Text.Label.Medium(
                        text = stringResource(id = R.string.season, season).uppercase(),
                    )
                },
                selected = isSelected,
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = "Selected icon",
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else { null },
            )

        }

    }

}

@FluxPreview
@Composable
fun MediaSeasonsTabs_Preview() {
    AppTheme {
        SeasonsTabs(
            selectedSeason = 2,
            seasons = listOf(1,2,3),
            onSeasonTap = {}
        )
    }
}