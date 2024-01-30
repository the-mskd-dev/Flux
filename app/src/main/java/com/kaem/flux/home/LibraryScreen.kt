package com.kaem.flux.home

import android.os.Build
import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.data.repository.SortOrder
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.utils.Constants

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen() {

    val viewModel = viewModel<LibraryViewModel>()
    val uiState by viewModel.libraryUiState.observeAsState()
    val permissionState = libraryPermissionState()

    if (!permissionState.status.isGranted) {

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            FluxButton(
                text = stringResource(id = R.string.give_permission),
                onClick = { permissionState.launchPermissionRequest() }
            )

        }

    } else {

        LaunchedEffect(Unit) {
            viewModel.getLibrary()
        }

        Crossfade(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            targetState = uiState?.isLoading ?: true,
            label = "LibraryAnimation"
        ) {

            when (it) {

                true -> Loader()
                false -> LibraryContent(
                    artworks = uiState?.artworks.orEmpty(),
                    onSortButtonTap = { s -> viewModel.applySort(s) }
                )

            }

        }

    }

}

@Composable
fun LibraryContent(
    artworks: List<FluxArtworkSummary>,
    onSortButtonTap: (SortOrder) -> Unit
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
            onSortButtonTap = onSortButtonTap
        )

    }

}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LibraryGrid(
    artworks: List<FluxArtworkSummary>,
    onSortButtonTap: (SortOrder) -> Unit
) {

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item(span = { GridItemSpan(3) }) {

            Spacer(
                modifier = Modifier
                    .statusBarsPadding()
                    .height(20.dp)
            )

        }

        item(span = { GridItemSpan(3) }) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                FluxButton(text = "Par nom") {
                    onSortButtonTap(SortOrder.NAME)
                }

                FluxButton(text = "Par date") {
                    onSortButtonTap(SortOrder.RELEASE_DATE)
                }

                FluxButton(text = "Par date d'ajout") {
                    onSortButtonTap(SortOrder.ADDED_DATE)
                }

            }

        }

        items(items = artworks, key = { it.id }) {

            LibraryArtwork(
                modifier = Modifier.animateItemPlacement(),
                artworkSummary = it
            )

        }

        item(span = { GridItemSpan(3) }) {

            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(20.dp)
            )

        }

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LibraryArtwork(
    modifier: Modifier,
    artworkSummary: FluxArtworkSummary
) {

    GlideImage(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .aspectRatio(2f / 3f),
        model = Constants.TMDB.IMAGE_SMALL + artworkSummary.imagePath,
        contentDescription = artworkSummary.title,
        loading = placeholder(ColorPainter(Color.LightGray))
    )

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun libraryPermissionState(
    viewModel: LibraryViewModel = viewModel()
): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_VIDEO
    else
        android.Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}