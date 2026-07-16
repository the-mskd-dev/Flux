package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import com.mskd.flux.R
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.itemWidthFor
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_add
import flux.shared.generated.resources.ic_rewind
import flux.shared.generated.resources.other_files
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
                        itemWidth = itemWidth,
                        onTap = { sendIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN)) },
                        painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_launcher_foreground)),
                        iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentDescription = stringResource(Res.string.other_files)
                    )

                }
            }

            item {

                CatalogGenericItem(
                    itemWidth = itemWidth,
                    onTap = {  },
                    painter = painterResource(Res.drawable.ic_add),
                    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentDescription = stringResource(Res.string.other_files)
                )

            }

        }

    }

}

@Composable
fun CatalogGenericItem(
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
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            colorFilter = ColorFilter.tint(iconColor),
            contentDescription = contentDescription
        )

    }

}