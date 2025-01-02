package com.kaem.flux.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaem.flux.R
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.screens.permissions.PermissionsScreen
import com.kaem.flux.screens.permissions.fluxPermissionState
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.ui.component.BoldText
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.theme.Dimensions
import com.kaem.flux.utils.Constants

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val permissions = fluxPermissionState()

    if (!permissions.status.isGranted) {

        PermissionsScreen { permissions.launchPermissionRequest() }

    } else {

        LaunchedEffect(Unit) {
            viewModel.getLibrary()
        }

        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = uiState.screenState,
            label = "LibraryAnimation"
        ) {

            when (it) {

                ScreenState.LOADING -> Loader()
                else -> {

                    HomeContent(
                        overviews = uiState.overviews,
                        lastWatchedIds = uiState.lastWatchedArtworkIds,
                        isSyncing = uiState.isSyncing,
                        onSyncTap = { viewModel.getLibrary(manualSync = true) },
                        navigateToDetails = { id -> navigateToDetails(id) },
                        navigateToCategory = { type -> navigateToCategory(type) },
                        navigateToSearch = navigateToSearch,
                        navigateToSettings = navigateToSettings
                    )

                }

            }

        }

    }

}

@Composable
fun HomeContent(
    overviews: List<ArtworkOverview>,
    lastWatchedIds: List<Long>,
    isSyncing: Boolean,
    onSyncTap: () -> Unit,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSettings: () -> Unit
) {

    if (overviews.isEmpty()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            //TODO
            Text(
                text = "No content",
                color = MaterialTheme.colorScheme.primary
            )

        }

    } else {

        HomeLists(
            overviews = overviews,
            lastWatchedIds = lastWatchedIds,
            isSyncing = isSyncing,
            onSyncTap = onSyncTap,
            navigateToDetails = navigateToDetails,
            navigateToCategory = navigateToCategory,
            navigateToSearch = navigateToSearch,
            navigateToSettings = navigateToSettings
        )

    }

}

@Composable
fun HomeLists(
    overviews: List<ArtworkOverview>,
    lastWatchedIds: List<Long>,
    isSyncing: Boolean,
    onSyncTap: () -> Unit,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSettings: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = Dimensions.Space.LARGE),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Space.MEDIUM)
    ) {

        HomeTopButtons(
            isSyncing = isSyncing,
            onSyncTap = onSyncTap,
            navigateToSearch = navigateToSearch,
            navigateToSettings = navigateToSettings
        )

        ArtworkList(
            overviews = lastWatchedIds.mapNotNull { overviews.find { o -> o.id == it } },
            largeArtwork = true,
            navigateToDetails = navigateToDetails
        )

        ArtworkList(
            name = stringResource(id = ContentType.SHOW.stringResource),
            overviews = overviews.filter { it.type == ContentType.SHOW },
            navigateToDetails = navigateToDetails,
            navigateToCategory = { navigateToCategory(ContentType.SHOW) }
        )

        ArtworkList(
            name = stringResource(id = ContentType.MOVIE.stringResource),
            overviews = overviews.filter { it.type == ContentType.MOVIE },
            navigateToDetails = navigateToDetails,
            navigateToCategory = { navigateToCategory(ContentType.MOVIE) }
        )

    }

}

@Composable
fun HomeTopButtons(
    isSyncing: Boolean,
    onSyncTap: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSettings: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Space.EXTRA_SMALL, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = navigateToSearch) {
            Icon(
                imageVector = Icons.Rounded.Search,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Search button"
            )
        }

        Crossfade(
            targetState = isSyncing,
            label = "Refresh indicator"
        ) { syncing ->
            if (syncing) {
                IconButton(onClick = {}) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

            } else {
                IconButton(onClick = onSyncTap) {
                    Icon(
                        painter = painterResource(R.drawable.sync),
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "Sync button"
                    )
                }
            }
        }

        IconButton(onClick = navigateToSettings) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Settings button"
            )
        }

    }

}

@Composable
fun ArtworkList(
    name: String? = null,
    largeArtwork: Boolean = false,
    overviews: List<ArtworkOverview>,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: () -> Unit = {}
) {

    if (overviews.isEmpty())
        return

    val width = if (largeArtwork) 450.dp else 120.dp
    val ratio = if (largeArtwork) 1920f/1080f else 2f/3f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Space.MEDIUM)
    ) {

        BoldText(
            modifier = Modifier
                .clickable { navigateToCategory() }
                .fillMaxWidth()
                .padding(start = Dimensions.Space.MEDIUM, top = Dimensions.Space.LARGE),
            text = name
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Dimensions.Space.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Space.SMALL)
        ) {

            items(overviews, key = { it.id }) {

                val url = if (largeArtwork) Constants.TMDB.IMAGE + it.bannerPath else Constants.TMDB.IMAGE_SMALL + it.imagePath

                ArtworkItem(
                    width = width,
                    ratio = ratio,
                    url = url,
                    onTap = { navigateToDetails(it.id) },
                    description = it.title
                )

            }

        }

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtworkItem(
    width: Dp,
    url: String,
    ratio: Float,
    onTap: () -> Unit,
    description: String
) {

    GlideImage(
        modifier = Modifier
            .clickable { onTap() }
            .clip(Dimensions.Shape.RoundedCorner)
            .width(width)
            .aspectRatio(ratio),
        model = url,
        contentDescription = description,
        loading = Placeholders.loading
    )

}