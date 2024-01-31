package com.kaem.flux.home

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryScreen() {

    val viewModel = viewModel<LibraryViewModel>()
    val uiState by viewModel.libraryUiState.observeAsState()
    val permissionState = libraryPermissionState()

    if (!permissionState.status.isGranted) {

        LibraryPermissionButton(permissionState = permissionState)

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
                    promotedArtworkIds = uiState?.promotedArtworkIds.orEmpty(),
                    sortOrder = uiState?.sortOrder ?: SortOrder.ADDED_DATE
                )

            }

        }

    }

}

@Composable
fun LibraryContent(
    artworks: List<FluxArtworkSummary>,
    promotedArtworkIds: List<Int>,
    sortOrder: SortOrder,
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
            promotedArtworkIds = promotedArtworkIds,
            sortOrder = sortOrder
        )

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryGrid(
    artworks: List<FluxArtworkSummary>,
    promotedArtworkIds: List<Int>,
    sortOrder: SortOrder,
) {

    val promotedArtwork = buildList {

        for (id in promotedArtworkIds)
            add(artworks.find { it.id == id })

    }.filterNotNull()

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        item(span = { GridItemSpan(3) }) {

            PromotedArtworks(artworks = promotedArtwork)

        }

        item(span = { GridItemSpan(3) }) {

            LibrarySortOrder(sortOrder = sortOrder)

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

@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun PromotedArtworks(
    artworks: List<FluxArtworkSummary>
) {

    val pagerState = rememberPagerState(pageCount = { artworks.size })

    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(8f / 9f),
        state = pagerState,
        key = { artworks[it].id }
    ) { page ->

        val artwork = artworks[page]

        GlideImage(
            modifier = Modifier.fillMaxSize(),
            model = Constants.TMDB.IMAGE + artwork.bannerPath,
            contentDescription = artwork.title,
            loading = placeholder(ColorPainter(Color.LightGray)),
            contentScale = ContentScale.Crop
        )

    }

}

@Composable
fun LibrarySortOrder(
    sortOrder: SortOrder,
    viewModel: LibraryViewModel = viewModel()
) {

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clickable { expanded = !expanded }
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {

        Text(
            modifier = Modifier
                .padding(vertical = 8.dp),
            text = stringResource(id = sortOrder.stringResId),
            fontWeight = FontWeight.W600,
            color = MaterialTheme.colorScheme.onBackground
        )

        AnimatedVisibility(visible = expanded) {

            Column {

                Text(
                    modifier = Modifier
                        .clickable { viewModel.applySort(SortOrder.NAME) }
                        .padding(vertical = 8.dp),
                    text = stringResource(id = SortOrder.NAME.stringResId),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    modifier = Modifier
                        .clickable { viewModel.applySort(SortOrder.RELEASE_DATE) }
                        .padding(vertical = 8.dp),
                    text = stringResource(id = SortOrder.RELEASE_DATE.stringResId),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    modifier = Modifier
                        .clickable { viewModel.applySort(SortOrder.ADDED_DATE) }
                        .padding(vertical = 8.dp),
                    text = stringResource(id = SortOrder.ADDED_DATE.stringResId),
                    color = MaterialTheme.colorScheme.onBackground
                )

            }


        }

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LibraryArtwork(
    modifier: Modifier,
    artworkSummary: FluxArtworkSummary,
    viewModel: LibraryViewModel = viewModel()
) {

    GlideImage(
        modifier = Modifier
            .then(modifier)
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clickable {

                viewModel.addWatchedArtwork(artworkSummary.id)

            },
        model = Constants.TMDB.IMAGE_SMALL + artworkSummary.imagePath,
        contentDescription = artworkSummary.title,
        loading = placeholder(ColorPainter(Color.LightGray))
    )

}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LibraryPermissionButton(permissionState: PermissionState) {

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