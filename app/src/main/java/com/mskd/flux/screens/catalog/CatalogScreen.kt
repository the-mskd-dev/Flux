package com.mskd.flux.screens.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.presentation.CatalogEvent
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.catalog.presentation.CatalogState
import com.mskd.flux.features.catalog.presentation.CatalogViewModel
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.screens.catalog.composable.CatalogEmptyContent
import com.mskd.flux.screens.catalog.composable.CatalogHeader
import com.mskd.flux.screens.catalog.composable.CatalogMenu
import com.mskd.flux.screens.catalog.composable.CatalogViewMenu
import com.mskd.flux.screens.catalog.composable.LastWatchedCarousel
import com.mskd.flux.screens.catalog.composable.sorting.CatalogSortingSheet
import com.mskd.flux.screens.catalog.composable.viewMode.CatalogViewModeSheet
import com.mskd.flux.screens.catalog.composable.viewMode.catalogViewModeGenre
import com.mskd.flux.screens.catalog.composable.viewMode.catalogViewModeGrid
import com.mskd.flux.screens.catalog.composable.viewMode.catalogViewModeType
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.sync_in_progress
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CatalogScreen(
    navigate: (Route) -> Unit,
    viewModel: CatalogViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CatalogEvent.NavigateToSearch -> navigate(Route.Search(withGenre = event.genre, withType = event.category))
                is CatalogEvent.NavigateToMovie -> navigate(Route.Artwork(artworkId = event.artworkId, season = null, rgb = event.rgb))
                is CatalogEvent.NavigateToShow -> navigate(Route.Show(artworkId = event.artworkId, rgb = event.rgb))
                CatalogEvent.NavigateToUnknown -> navigate(Route.UnknownArtworks)
                CatalogEvent.NavigateToHowTo -> navigate(Route.HowTo)
                CatalogEvent.NavigateToSettings -> navigate(Route.Settings)
                CatalogEvent.NavigateToToken -> navigate(Route.Token(fromSetup = false))
                CatalogEvent.NavigateToSources -> navigate(Route.Sources(fromSetup = false))
            }
        }
    }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = uiState.state,
        label = "PlayerScreenState",
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        contentKey = { state ->
            when (state) {
                CatalogState.Error -> "error"
                is CatalogState.Loading -> "loading"
                is CatalogState.Content -> "content"
            }
        }
    ) { state ->

        when (state) {
            is CatalogState.Loading -> {

                LoadingScreen(
                    text = stringResource(Res.string.sync_in_progress),
                    progress = { state.progress }
                )
            }

            is CatalogState.Content -> {

                CatalogContent(
                    artworks = state.artworks,
                    genres = state.genres,
                    lastWatchedIds = state.lastWatchedMediaIds,
                    isRefreshing = state.isRefreshing,
                    tokenIsMissing = state.tokenIsMissing,
                    sortingMode = state.sortingMode,
                    showSortingModes = state.showSortingSheet,
                    viewMode = state.viewMode,
                    showViewModes = state.showViewSheet,
                    sendIntent = viewModel::handleIntent
                )

            }

            else -> {}
        }

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CatalogContent(
    artworks: List<Artwork>,
    genres: List<Genre>,
    lastWatchedIds: List<Long>,
    isRefreshing: Boolean,
    tokenIsMissing: Boolean,
    sortingMode: CatalogSortingMode,
    showSortingModes: Boolean,
    viewMode: CatalogViewMode,
    showViewModes: Boolean,
    sendIntent: (CatalogIntent) -> Unit
) {

    val pullToRefreshState = rememberPullToRefreshState()
    var offsetY by remember { mutableFloatStateOf(0f) }
    val loaderAnim by animateFloatAsState(pullToRefreshState.distanceFraction.coerceIn(0f, 1f))
    with(LocalDensity.current) {
        offsetY = 100.dp.toPx() * pullToRefreshState.distanceFraction
    }

    val columns = FluxUI.itemsPerRow.artworks

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding()))

            CatalogHeader()

            PullToRefreshBox(
                modifier = Modifier.weight(1f),
                isRefreshing = isRefreshing,
                onRefresh = { sendIntent(CatalogIntent.SyncCatalog) },
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

                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationY = offsetY },
                    columns = GridCells.Fixed(columns),
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                    horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
                    contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
                ) {

                    if (artworks.none { !it.isUnknown }) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CatalogEmptyContent(sendIntent = sendIntent)
                        }
                    }

                    if (artworks.any { !it.isUnknown }) {

                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
                            ) {

                                LastWatchedCarousel(
                                    artworks = lastWatchedIds.mapNotNull { artworks.find { o -> o.id == it } },
                                    sendIntent = sendIntent
                                )

                                CatalogViewMenu(
                                    sortingMode = sortingMode,
                                    viewMode = viewMode,
                                    sendIntent = sendIntent
                                )

                            }
                        }

                        when (viewMode) {
                            CatalogViewMode.GRID -> {
                                catalogViewModeGrid(
                                    artworks = artworks,
                                    sendIntent = sendIntent
                                )
                            }
                            CatalogViewMode.BY_TYPE -> {
                                catalogViewModeType(
                                    artworks = artworks,
                                    sortingMode = sortingMode,
                                    sendIntent = sendIntent
                                )
                            }
                            CatalogViewMode.BY_GENRE -> {
                                catalogViewModeGenre(
                                    artworks = artworks,
                                    genres = genres,
                                    sortingMode = sortingMode,
                                    sendIntent = sendIntent
                                )
                            }
                        }

                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = FluxUI.Space.medium)
                                .padding(bottom = paddingValues.calculateBottomPadding() + FluxUI.Space.bottomScreen)
                        ) {

                            CatalogMenu(
                                artworks = artworks,
                                tokenIsMissing = tokenIsMissing,
                                sendIntent = sendIntent
                            )

                        }

                    }

                }

            }

        }


        if (showSortingModes) {
            CatalogSortingSheet(
                selectedMode = sortingMode,
                sendIntent = sendIntent
            )
        }

        if (showViewModes) {
            CatalogViewModeSheet(
                selectedMode = viewMode,
                sendIntent = sendIntent
            )
        }

    }


}

@FluxPreview
@Composable
fun CatalogScreen_Preview() {
    FluxThemePreview {
        Surface {
            CatalogContent(
                artworks = MediaMockups.artworks,
                genres = DetailsMockup.allGenres,
                lastWatchedIds = MediaMockups.artworks.map { it.id },
                isRefreshing = false,
                tokenIsMissing = false,
                sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                showSortingModes = false,
                viewMode = CatalogViewMode.BY_TYPE,
                showViewModes = false,
                sendIntent = {}
            )
        }
    }
}

@FluxPreview
@Composable
fun CatalogScreen_Unknown_Preview() {
    FluxThemePreview {
        Surface {
            CatalogContent(
                artworks = listOf(MediaMockups.unknownArtwork),
                genres = emptyList(),
                lastWatchedIds = emptyList(),
                isRefreshing = false,
                tokenIsMissing = true,
                sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                showSortingModes = false,
                viewMode = CatalogViewMode.BY_TYPE,
                showViewModes = false,
                sendIntent = {}
            )
        }
    }
}

@FluxPreview
@Composable
fun CatalogScreen_Empty_Preview() {
    FluxThemePreview {
        Surface {
            CatalogContent(
                artworks = emptyList(),
                genres = emptyList(),
                lastWatchedIds = emptyList(),
                isRefreshing = false,
                tokenIsMissing = true,
                sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                showSortingModes = false,
                viewMode = CatalogViewMode.BY_TYPE,
                showViewModes = false,
                sendIntent = {}
            )
        }
    }
}