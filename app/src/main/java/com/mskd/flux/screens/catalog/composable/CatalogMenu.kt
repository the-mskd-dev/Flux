package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add_source
import flux.shared.generated.resources.add_token
import flux.shared.generated.resources.ic_add_folder
import flux.shared.generated.resources.ic_api
import flux.shared.generated.resources.ic_flux
import flux.shared.generated.resources.other_files
import flux.shared.generated.resources.tmdb_api_token
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogMenu(
    artworks: List<Artwork>,
    tokenIsMissing: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    FlowRow(
        modifier = Modifier
            .padding(horizontal = FluxUI.Space.medium)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        //if (artworks.any { it.isUnknown }) {
            CatalogMenuItem(
                text = stringResource(Res.string.other_files),
                painter = painterResource(Res.drawable.ic_add_folder),
                iconColor = MaterialTheme.colorScheme.tertiary,
                onClick = { sendIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN)) }
            )
        //}

        //if (artworks.isEmpty()) {
            CatalogMenuItem(
                text = stringResource(Res.string.add_source),
                painter = painterResource(Res.drawable.ic_add_folder),
                iconColor = MaterialTheme.colorScheme.secondary,
                onClick = { sendIntent(CatalogIntent.OnSourcesTap) }
            )
        //}

        //if (tokenIsMissing) {
            CatalogMenuItem(
                text = stringResource(Res.string.add_token),
                painter = painterResource(Res.drawable.ic_api),
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { sendIntent(CatalogIntent.OnTokenTap) }
            )
        //}

    }

}

@Composable
fun CatalogMenuItem(
    text: String,
    painter: Painter,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(AssistChipDefaults.shape)
            .width(200.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .9f))
            .clickable { onClick() }
            .padding(all = FluxUI.Space.small),
        horizontalArrangement = AssistChipDefaults.horizontalArrangement(),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            modifier = Modifier.size(AssistChipDefaults.IconSize),
            painter = painter,
            tint = iconColor,
            contentDescription = text
        )

        Text.Content.Body(
            text = text,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            overflow = TextOverflow.Ellipsis
        )

    }

}

@FluxPreview
@Composable
fun CatalogMenu_Preview() {
    FluxThemePreview {
        Box(Modifier.fillMaxSize()) {
            CatalogMenu(
                artworks = MediaMockups.artworks,
                tokenIsMissing = true,
                sendIntent = {}
            )
        }
    }
}