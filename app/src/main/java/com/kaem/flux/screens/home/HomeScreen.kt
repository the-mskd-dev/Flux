package com.kaem.flux.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
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
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight
import com.kaem.flux.utils.Constants

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navigateToDetails: (Long) -> Unit,
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

                    val state = rememberPullToRefreshState()

                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        contentAlignment = Alignment.TopCenter,
                        isRefreshing = uiState.isSyncing,
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = uiState.isSyncing,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                state = state
                            )
                        },
                        onRefresh = { viewModel.getLibrary(manualSync = true) }
                    ) {

                        HomeContent(
                            overviews = uiState.overviews,
                            lastWatchedIds = uiState.lastWatchedArtworkIds,
                            navigateToDetails = { id -> navigateToDetails(id) },
                            navigateToCategory = {
                                //TODO
                            }
                        )

                    }

                }

            }

        }

    }

}

@Composable
fun HomeContent(
    overviews: List<ArtworkOverview>,
    lastWatchedIds: List<Long>,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit
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
            navigateToDetails = { navigateToDetails(it) },
            navigateToCategory = navigateToCategory
        )

    }

}

@Composable
fun HomeLists(
    overviews: List<ArtworkOverview>,
    lastWatchedIds: List<Long>,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
            .padding(bottom = FluxSpace.LARGE),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        ArtworkList(
            overviews = lastWatchedIds.mapNotNull { overviews.find { o -> o.id == it } },
            largeArtwork = true,
            navigateToDetails = navigateToDetails
        )

        ArtworkList(
            name = stringResource(id = R.string.shows),
            overviews = overviews.filter { it.type == ContentType.SHOW },
            navigateToDetails = navigateToDetails,
            navigateToCategory = { navigateToCategory(ContentType.SHOW) }
        )

        ArtworkList(
            name = stringResource(id = R.string.movies),
            overviews = overviews.filter { it.type == ContentType.MOVIE },
            navigateToDetails = navigateToDetails,
            navigateToCategory = { navigateToCategory(ContentType.MOVIE) }
        )

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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        name?.let {
            Text(
                modifier = Modifier
                    .clickable { navigateToCategory() }
                    .padding(start = FluxSpace.MEDIUM, top = FluxSpace.LARGE),
                text = name,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FluxWeight.BOLD,
                fontSize = FluxFontSize.LARGE,
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = FluxSpace.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(FluxSpace.SMALL)
        ) {

            items(overviews, key = { it.id }) {

                ArtworkItem(
                    modifier = Modifier.clickable { navigateToDetails(it.id) },
                    overview = it,
                    largeArtwork = largeArtwork
                )

            }

        }

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtworkItem(
    modifier: Modifier = Modifier,
    overview: ArtworkOverview,
    largeArtwork: Boolean = false
) {

    val width = if (largeArtwork) 450.dp else 120.dp
    val ratio = if (largeArtwork) 1920f/1080f else 2f/3f
    val url = if (largeArtwork) Constants.TMDB.IMAGE + overview.bannerPath else Constants.TMDB.IMAGE_SMALL + overview.imagePath

    GlideImage(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .width(width)
            .aspectRatio(ratio),
        model = url,
        contentDescription = overview.title,
        loading = Placeholders.loading
    )

}