package com.mskd.flux.features.people.ui.components.person

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.people.presentation.PeopleIntent
import com.mskd.flux.ui.bleedHorizontal
import com.mskd.flux.ui.dimensions.itemWidthFor
import com.mskd.flux.ui.media.MediaItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.known_for
import org.jetbrains.compose.resources.stringResource

@Composable
fun PersonArtworks(
    artworks: List<Artwork>,
    sendIntent: (PeopleIntent) -> Unit
) {

    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }
    val density = LocalDensity.current

    Column(
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text.Content.Title(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.known_for),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    with(density) {
                        itemWidth = itemWidthFor(
                            screenWidthDp = size.width.toDp(),
                            columns = 3
                        )
                    }
                }
                .bleedHorizontal(),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            items(items = artworks, key = { it.id }) { artwork ->

                MediaItem(
                    modifier = Modifier
                        .width(itemWidth)
                        .widthIn(max = FluxUI.Dimension.itemWidth),
                    path = artwork.imagePath,
                    description = artwork.title,
                    onClick = { rgb -> sendIntent(PeopleIntent.OnArtworkClick(artwork = artwork, rgb = rgb)) }
                )

            }

        }

    }

}