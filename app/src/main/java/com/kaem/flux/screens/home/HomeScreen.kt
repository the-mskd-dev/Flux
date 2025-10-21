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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.ScreenState
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.screens.welcome.WelcomeScreen
import com.kaem.flux.screens.welcome.fluxPermissionState
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.FluxTextButton
import com.kaem.flux.ui.component.Image
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.component.Text.Body.Large
import com.kaem.flux.ui.component.Text.Title.Large
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToHowTo: () -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val permissions = fluxPermissionState()

    if (!permissions.status.isGranted) {

        WelcomeScreen { permissions.launchPermissionRequest() }

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
                        lastWatchedIds = uiState.lastWatchedMediaIds,
                        isSyncing = uiState.isSyncing,
                        onSyncTap = { viewModel.getLibrary(manualSync = true) },
                        navigateToDetails = { id -> navigateToDetails(id) },
                        navigateToCategory = { type -> navigateToCategory(type) },
                        navigateToSearch = navigateToSearch,
                        navigateToHowTo = navigateToHowTo,
                        navigateToSettings = navigateToSettings
                    )

                }

            }

        }

    }

}

@Composable
fun HomeContent(
    overviews: List<MediaOverview>,
    lastWatchedIds: List<Long>,
    isSyncing: Boolean,
    onSyncTap: () -> Unit,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToHowTo: () -> Unit,
    navigateToSettings: () -> Unit
) {

    if (overviews.isEmpty()) {

        HomeEmpty(
            navigateToHowTo = navigateToHowTo,
            onReloadTap = onSyncTap
        )

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
fun HomeEmpty(
    navigateToHowTo: () -> Unit,
    onReloadTap: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Ui.Space.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE.times(2), Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text.Headline.Medium(text = stringResource(R.string.empty_library))

        Text.Body.Large(text = stringResource(R.string.empty_library_desc))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            FluxButton(
                text = stringResource(R.string.how_to_name_files),
                onTap = navigateToHowTo
            )

            FluxTextButton(
                text = stringResource(R.string.refresh),
                onTap = onReloadTap
            )

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeLists(
    overviews: List<MediaOverview>,
    lastWatchedIds: List<Long>,
    isSyncing: Boolean,
    onSyncTap: () -> Unit,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: (ContentType) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSettings: () -> Unit
) {

    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isSyncing,
        onRefresh = onSyncTap,
        state = state,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = state,
                isRefreshing = isSyncing,
                maxDistance = 120.dp
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(bottom = Ui.Space.LARGE),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            HomeTopButtons(
                navigateToSearch = navigateToSearch,
                navigateToSettings = navigateToSettings
            )

            LastWatchedCarousel(
                overviews = lastWatchedIds.mapNotNull { overviews.find { o -> o.id == it } },
                navigateToDetails = navigateToDetails
            )

            MediaList(
                name = stringResource(id = ContentType.SHOW.stringResource),
                overviews = overviews.filter { it.type == ContentType.SHOW },
                navigateToDetails = navigateToDetails,
                navigateToCategory = { navigateToCategory(ContentType.SHOW) }
            )

            MediaList(
                name = stringResource(id = ContentType.MOVIE.stringResource),
                overviews = overviews.filter { it.type == ContentType.MOVIE },
                navigateToDetails = navigateToDetails,
                navigateToCategory = { navigateToCategory(ContentType.MOVIE) }
            )

        }

    }

}

@Composable
fun HomeTopButtons(
    navigateToSearch: () -> Unit,
    navigateToSettings: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = navigateToSearch) {
            Icon(
                imageVector = Icons.Rounded.Search,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Search button"
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LastWatchedCarousel(
    overviews: List<MediaOverview>,
    navigateToDetails: (Long) -> Unit,
) {

    if (overviews.isEmpty())
        return

    val ratio = 1920f/1080f

    val carouselState = rememberCarouselState { overviews.size }

    HorizontalCenteredHeroCarousel(
        modifier = Modifier.fillMaxWidth(),
        maxItemWidth = 350.dp,
        state = carouselState,
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM)
    ) { i ->

        val overview = overviews[i]
        val url = Constants.TMDB.IMAGE + overview.bannerPath

        Image(
            modifier = Modifier
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable { navigateToDetails(overview.id) }
                .aspectRatio(ratio),
            url = url,
            contentDescription = overview.title
        )

    }

}

@Composable
fun MediaList(
    name: String? = null,
    overviews: List<MediaOverview>,
    navigateToDetails: (Long) -> Unit,
    navigateToCategory: () -> Unit = {}
) {

    if (overviews.isEmpty())
        return

    val width = 120.dp
    val ratio = 2f/3f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Text.Title.Large(
            modifier = Modifier
                .clickable { navigateToCategory() }
                .fillMaxWidth()
                .padding(start = Ui.Space.MEDIUM, top = Ui.Space.LARGE),
            text = name
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            items(overviews, key = { it.id }) {

                MediaItem(
                    width = width,
                    ratio = ratio,
                    url = Constants.TMDB.IMAGE_SMALL + it.imagePath,
                    onTap = { navigateToDetails(it.id) },
                    description = it.title
                )

            }

        }

    }
}

@Composable
fun MediaItem(
    width: Dp,
    url: String,
    ratio: Float,
    onTap: () -> Unit,
    description: String
) {

    Image(
        modifier = Modifier
            .clickable { onTap() }
            .clip(Ui.Shape.RoundedCorner)
            .width(width)
            .aspectRatio(ratio),
        url = url,
        contentDescription = description
    )

}

@Preview
@Composable
fun HomeScreen_Preview() {
    FluxTheme(theme = Ui.THEME.DARK) {
        HomeContent(
            navigateToDetails = {},
            navigateToCategory = {},
            navigateToSearch = {},
            navigateToHowTo = {},
            navigateToSettings = {},
            onSyncTap = {},
            overviews = MediaMockups.overviews,
            lastWatchedIds = MediaMockups.overviews.map { it.id },
            isSyncing = false
        )
    }
}