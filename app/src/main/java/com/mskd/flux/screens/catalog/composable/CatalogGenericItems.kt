package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.itemWidthFor
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_add_folder
import flux.shared.generated.resources.ic_flux
import flux.shared.generated.resources.other_files
import flux.shared.generated.resources.sources
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogGenericItems(
    showUnknown: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    val density = LocalDensity.current
    val columns = FluxUI.itemsPerRow.artworks
    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                with(density) {
                    itemWidth = itemWidthFor(
                        screenWidthDp = size.width.toDp(),
                        columns = columns
                    )
                }
            },
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
    ) {

        Text.Title.Large(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = FluxUI.Space.medium, top = FluxUI.Space.large),
            text = stringResource(Res.string.other_files),
            emphasized = true,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            if (showUnknown) {
                item {

                    CatalogGenericItem(
                        modifier = Modifier.size(48.dp),
                        itemWidth = itemWidth,
                        onTap = { sendIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN)) },
                        painter = painterResource(Res.drawable.ic_flux),
                        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentDescription = stringResource(Res.string.other_files)
                    )

                }
            }

            item {

                CatalogGenericItem(
                    modifier = Modifier.size(48.dp),
                    itemWidth = itemWidth,
                    onTap = { sendIntent(CatalogIntent.OnSourcesTap) },
                    painter = painterResource(Res.drawable.ic_add_folder),
                    iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentDescription = stringResource(Res.string.sources)
                )

            }

        }

    }

}

@Composable
fun CatalogGenericItem(
    modifier: Modifier,
    itemWidth: Dp,
    painter: Painter,
    iconColor: Color,
    backgroundColor: Color,
    onTap: () -> Unit,
    contentDescription: String,
) {

    Box(
        modifier = Modifier
            .clickable { onTap() }
            .clip(FluxUI.shapes.corners)
            .width(itemWidth)
            .aspectRatio(FluxUI.Dimension.itemRatio)
            .background(color = backgroundColor),
        contentAlignment = Alignment.Center
    ) {

        Image(
            modifier = modifier,
            painter = painter,
            colorFilter = ColorFilter.tint(iconColor),
            contentDescription = contentDescription
        )

    }

}

@Preview
@Composable
fun CatalogGenericItems_Preview() {
    FluxTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            CatalogGenericItems(
                showUnknown = true,
                sendIntent = {}
            )
        }
    }
}