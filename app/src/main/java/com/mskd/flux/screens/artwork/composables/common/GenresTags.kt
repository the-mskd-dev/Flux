package com.mskd.flux.screens.artwork.composables.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview

@Composable
fun GenresTags(genres: List<Genre>) {

    FlowRow(
        itemVerticalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {
        genres.forEach { genre ->
            Text.Card.Label(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(vertical = FluxUI.Space.extraSmall, horizontal = FluxUI.Space.small),
                text = genre.name,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }

}

@Preview
@Composable
fun GenresTags_Preview() {
    FluxThemePreview {
        GenresTags(genres = DetailsMockup.allGenres)
    }
}