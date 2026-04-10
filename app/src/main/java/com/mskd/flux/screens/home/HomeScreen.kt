package com.mskd.flux.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.navigation.Route
import com.mskd.flux.screens.howTo.HowToNameFiles
import com.mskd.flux.ui.component.FluxButton
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.MediaItem
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.tmdbImage
import com.mskd.flux.utils.extensions.tmdbImageLarge

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navigate: (Route) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is HomeEvent.NavigateToCategory -> navigate(Route.Search(contentType = event.category))
                is HomeEvent.NavigateToArtwork -> navigate(Route.Artwork(event.artworkId))
                HomeEvent.NavigateToUnknown -> navigate(Route.UnknownArtworks)
                HomeEvent.NavigateToHowTo -> navigate(Route.HowTo)
                HomeEvent.NavigateToSearch -> navigate(Route.Search())
                HomeEvent.NavigateToSettings -> navigate(Route.Settings)
            }
        }
    }

    Crossfade(
        modifier = Modifier.fillMaxSize(),
        targetState = uiState.screenState,
        label = "CatalogAnimation"
    ) {

        when (it) {

            ScreenState.LOADING -> LoadingScreen()

            else -> {

                if (uiState.artworks.isEmpty()) {

                    HomeEmpty(sendIntent = viewModel::handleIntent)

                } else {

                    HomeContent(
                        artworks = uiState.artworks,
                        lastWatchedIds = uiState.lastWatchedMediaIds,
                        isRefreshing = uiState.isRefreshing,
                        sendIntent = viewModel::handleIntent
                    )

                }



            }

        }

    }

}

@Composable
fun HomeEmpty(sendIntent: (HomeIntent) -> Unit) {

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(all = Ui.Space.MEDIUM)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE),
            horizontalAlignment = Alignment.Start,
        ) {

            Text.Headline.Medium(text = stringResource(R.string.empty_catalog))

            Text.Body.Large(text = stringResource(R.string.empty_catalog_desc))

            HowToNameFiles()

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FluxButton(
                    text = stringResource(R.string.refresh),
                    onTap = { sendIntent(HomeIntent.SyncCatalog) }
                )
            }


        }

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeContent(
    artworks: List<Artwork>,
    lastWatchedIds: List<Long>,
    isRefreshing: Boolean,
    sendIntent: (HomeIntent) -> Unit
) {

    val pullToRefreshState = rememberPullToRefreshState()
    var offsetY by remember { mutableFloatStateOf(0f) }
    val loaderAnim by animateFloatAsState(pullToRefreshState.distanceFraction.coerceIn(0f, 1f))
    with(LocalDensity.current) {
        offsetY = 100.dp.toPx() * pullToRefreshState.distanceFraction
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        HomeTopButtons(sendIntent = sendIntent)

        PullToRefreshBox(
            modifier = Modifier.weight(1f),
            isRefreshing = isRefreshing,
            onRefresh = { sendIntent(HomeIntent.SyncCatalog) },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    modifier = Modifier
                        .scale(loaderAnim)
                        .align(Alignment.TopCenter),
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing
                )
            }
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationY = offsetY },
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                item {
                    LastWatchedCarousel(
                        artworks = lastWatchedIds.mapNotNull { artworks.find { o -> o.id == it } },
                        sendIntent = sendIntent
                    )
                }

                item {
                    MediaCategory(
                        name = stringResource(id = ContentType.SHOW.stringResource),
                        category = ContentType.SHOW,
                        artworks = artworks.filter { it.type == ContentType.SHOW && !it.isUnknown },
                        sendIntent = sendIntent
                    )
                }

                item {
                    MediaCategory(
                        name = stringResource(id = ContentType.MOVIE.stringResource),
                        category = ContentType.MOVIE,
                        artworks = artworks.filter { it.type == ContentType.MOVIE && !it.isUnknown },
                        sendIntent = sendIntent
                    )
                }

                if (artworks.any { it.isUnknown }) {
                    item {
                        UnknownCategory(sendIntent = sendIntent)
                    }
                }

                item {
                    Spacer(
                        Modifier
                            .navigationBarsPadding()
                            .size(Ui.Space.LARGE)
                    )
                }

            }

        }

    }

}

@Composable
fun HomeTopButtons(sendIntent: (HomeIntent) -> Unit) {

    Row(
        modifier = Modifier
            .padding(vertical = Ui.Space.SMALL)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = { sendIntent(HomeIntent.OnSearchTap) }) {
            Icon(
                imageVector = Icons.Rounded.Search,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "Search button"
            )
        }

        IconButton(onClick = { sendIntent(HomeIntent.OnSettingsTap) }) {
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
    artworks: List<Artwork>,
    sendIntent: (HomeIntent) -> Unit
) {

    if (artworks.isEmpty())
        return

    val ratio = 1920f/1080f

    val carouselState = rememberCarouselState { artworks.size }

    HorizontalCenteredHeroCarousel(
        modifier = Modifier.fillMaxWidth(),
        maxItemWidth = 350.dp,
        state = carouselState,
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM)
    ) { i ->

        val overview = artworks[i]
        val url = overview.bannerPath.tmdbImageLarge

        Image(
            modifier = Modifier
                .maskClip(MaterialTheme.shapes.extraLarge)
                .clickable { sendIntent(HomeIntent.OnArtworkTap(artworkId = overview.id)) }
                .aspectRatio(ratio),
            url = url,
            contentDescription = overview.title
        )

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaCategory(
    name: String? = null,
    category: ContentType,
    artworks: List<Artwork>,
    sendIntent: (HomeIntent) -> Unit
) {

    if (artworks.isEmpty())
        return

    val width = 120.dp
    val ratio = 2f/3f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Text.Title.Large(
            modifier = Modifier
                .clickable { sendIntent(HomeIntent.OnCategoryTap(category)) }
                .fillMaxWidth()
                .padding(start = Ui.Space.MEDIUM, top = Ui.Space.LARGE),
            text = name,
            emphasized = true,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
        ) {

            items(artworks, key = { it.id }) {

                MediaItem(
                    width = width,
                    ratio = ratio,
                    url = it.imagePath.tmdbImage,
                    onTap = { sendIntent(HomeIntent.OnArtworkTap(artworkId = it.id)) },
                    description = it.title
                )

            }

        }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UnknownCategory(sendIntent: (HomeIntent) -> Unit) {

    val width = 120.dp
    val ratio = 2f/3f
    val foregroundPainter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.ic_launcher_foreground))
    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
    ) {

        Text.Title.Large(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Ui.Space.MEDIUM, top = Ui.Space.LARGE),
            text = stringResource(R.string.my_files),
            emphasized = true,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .padding(horizontal = Ui.Space.MEDIUM)
                .clickable { sendIntent(HomeIntent.OnArtworkTap(artworkId = Artwork.UNKNOWN_ID)) }
                .clip(Ui.Shape.Corner.Small)
                .width(width)
                .aspectRatio(ratio)
                .background(brush = backgroundGradient),
            contentAlignment = Alignment.Center
        ) {

            Image(
                modifier = Modifier.fillMaxSize(),
                painter = foregroundPainter,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                contentDescription = stringResource(R.string.my_files)
            )

        }

    }
}

@FluxPreview
@Composable
fun HomeScreen_Preview() {
    AppTheme {
        Surface {
            HomeContent(
                artworks = MediaMockups.artworks,
                lastWatchedIds = MediaMockups.artworks.map { it.id },
                isRefreshing = false,
                sendIntent = {}
            )
        }
    }
}

@FluxPreview
@Composable
fun HomeEmpty_Preview() {
    AppTheme {
        HomeEmpty(
            sendIntent = {}
        )
    }
}