package com.kaem.flux.screens.home

import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.utils.Constants

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen(
    navigateToDetails: (Int) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {

    val uiState by viewModel.libraryUiState.observeAsState()
    val permissionState = libraryPermissionState()

    if (!permissionState.status.isGranted) {

        LibraryPermissionButton(permissionState = permissionState)

    } else {

        LaunchedEffect(Unit) {

            if (uiState == null)
                viewModel.getLibrary()

        }

        Crossfade(
            modifier = Modifier.fillMaxSize(),
            targetState = uiState,
            label = "LibraryAnimation"
        ) {

            when (it) {

                null -> Loader()
                else -> LibraryContent(
                    artworks = it.artworks,
                    episodes = it.episodes,
                    lastWatchedIds = it.lastWatchedArtworkIds,
                    navigateToDetails = { id -> navigateToDetails(id) }
                )

            }

        }

    }

}

@Composable
fun LibraryContent(
    artworks: List<FluxArtwork>,
    episodes: List<FluxEpisode>,
    lastWatchedIds: List<Int>,
    navigateToDetails: (Int) -> Unit
) {

    if (artworks.isEmpty()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = "No content",
                color = MaterialTheme.colorScheme.primary
            )

        }

    } else {

        LibraryGrid(
            artworks = artworks,
            episodes = episodes,
            lastWatchedIds = lastWatchedIds,
            navigateToDetails = { navigateToDetails(it) }
        )

    }

}

@Composable
fun LibraryGrid(
    artworks: List<FluxArtwork>,
    episodes: List<FluxEpisode>,
    lastWatchedIds: List<Int>,
    navigateToDetails: (Int) -> Unit,
    viewModel: LibraryViewModel = viewModel()
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        Spacer(
            modifier = Modifier
                .statusBarsPadding()
                .height(FluxSpace.MEDIUM)
        )

        ArtworkList(
            artworks = artworks.filter { lastWatchedIds.contains(it.id) },
            largeArtwork = true,
            navigateToDetails = navigateToDetails
        )

        ArtworkList(
            name = stringResource(id = R.string.last_added),
            artworks = viewModel.getArtworksByAddedDate(artworks = artworks, episodes = episodes),
            navigateToDetails = navigateToDetails
        )

        ArtworkList(
            name = stringResource(id = R.string.shows),
            artworks = artworks.filterIsInstance<FluxShow>(),
            navigateToDetails = navigateToDetails
        )

        ArtworkList(
            name = stringResource(id = R.string.movies),
            artworks = artworks.filterIsInstance<FluxMovie>(),
            navigateToDetails = navigateToDetails
        )

        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .height(FluxSpace.LARGE)
        )

    }

}

@Composable
fun ArtworkList(
    name: String? = null,
    largeArtwork: Boolean = false,
    artworks: List<FluxArtwork>,
    navigateToDetails: (Int) -> Unit
) {

    if (artworks.isEmpty())
        return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        Title(
            modifier = Modifier.padding(start = FluxSpace.MEDIUM),
            text = name
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = FluxSpace.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(FluxSpace.SMALL)
        ) {

            items(artworks, key = { it.id }) {

                LibraryArtwork(
                    modifier = Modifier.clickable { navigateToDetails(it.id) },
                    artworkSummary = it,
                    largeArtwork = largeArtwork
                )

            }

        }

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LibraryArtwork(
    modifier: Modifier = Modifier,
    artworkSummary: FluxArtwork,
    largeArtwork: Boolean = false
) {

    val width = if (largeArtwork) 450.dp else 120.dp
    val ratio = if (largeArtwork) 1920f/1080f else 2f/3f
    val url = if (largeArtwork) Constants.TMDB.IMAGE + artworkSummary.bannerPath else Constants.TMDB.IMAGE_SMALL + artworkSummary.imagePath

    GlideImage(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(8.dp))
            .width(width)
            .aspectRatio(ratio),
        model = url,
        contentDescription = artworkSummary.title,
        loading = placeholder(ColorPainter(Color.LightGray))
    )

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryPermissionButton(permissionState: PermissionState) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        FluxButton(
            text = stringResource(id = R.string.give_permission),
            onClick = { permissionState.launchPermissionRequest() }
        )

    }

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun libraryPermissionState(): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_VIDEO
    else
        android.Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}