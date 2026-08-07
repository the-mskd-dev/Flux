package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogMenu(
    artworks: List<Artwork>,
    tokenIsMissing: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        if (artworks.any { it.isUnknown }) {
            CatalogMenuItem(
                text = stringResource(Res.string.other_files),
                painter = painterResource(Res.drawable.ic_flux),
                iconColor = MaterialTheme.colorScheme.tertiary,
                onClick = { sendIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN)) }
            )
        }

        if (artworks.isEmpty()) {
            CatalogMenuItem(
                text = stringResource(Res.string.add_source),
                painter = painterResource(Res.drawable.ic_add_folder),
                iconColor = MaterialTheme.colorScheme.secondary,
                onClick = { sendIntent(CatalogIntent.OnSourcesTap) }
            )
        }

        if (tokenIsMissing) {
            CatalogMenuItem(
                text = stringResource(Res.string.add_token),
                painter = painterResource(Res.drawable.ic_api),
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = { sendIntent(CatalogIntent.OnTokenTap) }
            )
        }

    }

}

@Composable
fun CatalogMenuItem(
    text: String,
    painter: Painter,
    iconColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = CircleShape,
        shadowElevation = FluxUI.Elevation.itemShadow,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FluxUI.Space.medium, vertical = 12.dp),
            horizontalArrangement = AssistChipDefaults.horizontalArrangement(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Icon(
                modifier = Modifier.size(24.dp),
                painter = painter,
                tint = iconColor,
                contentDescription = text
            )

            Text.Button.Default(
                text = text,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

        }

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