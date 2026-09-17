package com.mskd.flux.screens.privateFolder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderIntent
import com.mskd.flux.features.privateFolder.presentation.PrivateFolderUiState
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.component.global.FluxDropDownMenu
import com.mskd.flux.ui.component.global.FluxDropDownMenuItem
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.rememberScreenDimensions
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.remove_from_private_folder
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivateContentGrid(
    state: PrivateFolderUiState,
    innerPadding: PaddingValues,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

    val screenDimensions = rememberScreenDimensions()
    val columns = if (screenDimensions.isLarge) 5 else FluxUI.itemsPerRow.artworks

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        columns = GridCells.Fixed(columns),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + FluxUI.Space.medium,
            bottom = innerPadding.calculateBottomPadding() + FluxUI.Space.bottomScreen,
            start = FluxUI.Space.medium,
            end = FluxUI.Space.medium
        )
    ) {

        items(items = state.artworks, key = { it.id }) { artwork ->

            PrivateArtworkItem(
                modifier = Modifier.animateItem(),
                artwork = artwork,
                sendIntent = sendIntent
            )

        }

    }

}

@Composable
fun PrivateArtworkItem(
    modifier: Modifier,
    artwork: Artwork,
    sendIntent: (PrivateFolderIntent) -> Unit
) {

    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        MediaItem(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(FluxUI.Dimension.itemRatio),
            path = artwork.imagePath,
            onClick = { rgb -> sendIntent(PrivateFolderIntent.OnArtworkTap(artwork = artwork, rgb = rgb)) },
            onLongClick = { menuExpanded = true },
            description = artwork.title
        )

        if (menuExpanded) {

            FluxDropDownMenu(
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    FluxDropDownMenuItem(
                        text = stringResource(Res.string.remove_from_private_folder),
                        onClick = {
                            sendIntent(PrivateFolderIntent.RemoveFromPrivateFolder(artwork = artwork))
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = null
                            )
                        }
                    )
                )
            )

        }

    }

}

@FluxPreview
@Composable
fun PrivateScreen_Preview() {
    FluxTheme {
        PrivateScreenContent(
            state = PrivateFolderUiState(
                locked = false,
                artworks = MediaMockups.artworks.toImmutableList()
            ),
            sendIntent = {}
        )
    }
}
