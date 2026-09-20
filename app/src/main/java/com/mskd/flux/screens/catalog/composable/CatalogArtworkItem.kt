package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.media.MediaItem
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add_to_private_folder
import flux.shared.generated.resources.ic_lock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CatalogArtworkItem(
    modifier: Modifier,
    artwork: Artwork,
    privateFolderEnabled: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }

    val addToPrivateFolder = stringResource(Res.string.add_to_private_folder)

    Box(modifier = modifier) {

        MediaItem(
            modifier = Modifier.fillMaxSize(),
            path = artwork.imagePath,
            onClick = { rgb -> sendIntent(CatalogIntent.OnArtworkTap(artwork = artwork, rgb = rgb)) },
            onLongClick = { if (privateFolderEnabled) menuExpanded = true },
            description = artwork.title
        )

        if (menuExpanded && privateFolderEnabled) {

            FluxDropDownMenu(
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    FluxDropDownMenuItem(
                        text = addToPrivateFolder,
                        onClick = {
                            sendIntent(CatalogIntent.AddArtworkToPrivateFolder(artwork = artwork))
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_lock),
                                contentDescription = null
                            )
                        }
                    )
                )
            )

        }

    }

}
