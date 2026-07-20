package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_flux
import org.jetbrains.compose.resources.painterResource

@Composable
fun CatalogTopButtons(sendIntent: (CatalogIntent) -> Unit) {

    Row(
        modifier = Modifier
            .padding(vertical = FluxUI.Space.small, horizontal = FluxUI.Space.small)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = { sendIntent(CatalogIntent.OnSearchTap) }) {
            Icon(
                imageVector = Icons.Rounded.Search,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Search button"
            )
        }

        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(Res.drawable.ic_flux),
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Flux icon"
        )


        IconButton(onClick = { sendIntent(CatalogIntent.OnSettingsTap) }) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Settings button"
            )
        }

    }

}