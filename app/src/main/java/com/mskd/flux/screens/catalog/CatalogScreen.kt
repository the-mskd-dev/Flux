package com.mskd.flux.screens.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.presentation.CatalogEvent
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.catalog.presentation.CatalogMessageState
import com.mskd.flux.features.catalog.presentation.CatalogState
import com.mskd.flux.features.catalog.presentation.CatalogViewModel
import com.mskd.flux.features.history.data.mapper.toHistoryEntry
import com.mskd.flux.features.player.domain.model.PlayerParams
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.navigation.domain.Route.Player
import com.mskd.flux.presentation.animateAlphaState
import com.mskd.flux.presentation.blurBackground
import com.mskd.flux.presentation.blurForeground
import com.mskd.flux.screens.catalog.composable.CatalogEmptyContent
import com.mskd.flux.screens.catalog.composable.CatalogHeader
import com.mskd.flux.screens.catalog.composable.CatalogMenu
import com.mskd.flux.screens.catalog.composable.CatalogViewMenu
import com.mskd.flux.screens.catalog.composable.history.CatalogHistory
import com.mskd.flux.screens.catalog.composable.message.CatalogMessage
import com.mskd.flux.screens.catalog.composable.sorting.CatalogSortingSheet
import com.mskd.flux.screens.catalog.composable.viewMode.CatalogViewModeSheet
import com.mskd.flux.screens.catalog.composable.viewMode.catalogViewModeGenre
import com.mskd.flux.screens.catalog.composable.viewMode.catalogViewModeGrid
import com.mskd.flux.screens.catalog.composable.viewMode.catalogViewModeType
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.rememberExternalPlayerAction
import com.mskd.flux.utils.rememberScreenDimensions
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CatalogScreen(
    navigate: (Route) -> Unit,
    viewModel: CatalogViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val launchExternalPlayer = rememberExternalPlayerAction(
        onProgressResult = { progress -> viewModel.handleIntent(CatalogIntent.OnExternalPlayerResult(progress = progress)) },
        onFallbackToInternal = { media -> viewModel.handleIntent(CatalogIntent.PlayMedia(media = media, forceInternal = true)) }
    )

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
                CatalogEvent.NavigateToPrivateFolder -> navigate(Route.PrivateFolder)
                CatalogEvent.NavigateToMessage -> navigate(Route.Message)

                is CatalogEvent.PlayMedia -> {
                    if (event.externalPlayer)
                        launchExternalPlayer(event.media)
                    else
                        navigate(Player(params = PlayerParams.fromMedia(event.media)))
                }
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
                    text = state.syncState.description,
                    progress = { state.syncState.progress }
                )
            }

            is CatalogState.Content -> {

                CatalogContent(
                    state = state,
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
    state: CatalogState.Content,
    sendIntent: (CatalogIntent) -> Unit
) {

    val pullToRefreshState = rememberPullToRefreshState()
    var offsetY by remember { mutableFloatStateOf(0f) }
    val loaderAnim by animateFloatAsState(pullToRefreshState.distanceFraction.coerceIn(0f, 1f))
    with(LocalDensity.current) {
        offsetY = 100.dp.toPx() * pullToRefreshState.distanceFraction
    }

    val screenDimensions = rememberScreenDimensions()
    val columns = if (screenDimensions.isLarge) 5 else FluxUI.itemsPerRow.artworks

    val gridState = rememberLazyGridState()
    val isScrolled by remember { derivedStateOf { gridState.canScrollBackward } }
    val blurAlpha by animateAlphaState(targetValue = if (isScrolled) 1f else 0f,)

    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(0.dp) }
    val hazeState = rememberHazeState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {

            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .blurBackground(state = hazeState),
                isRefreshing = state.isRefreshing,
                onRefresh = { sendIntent(CatalogIntent.SyncCatalog) },
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        modifier = Modifier
                            .padding(top = headerHeight)
                            .scale(loaderAnim)
                            .align(Alignment.TopCenter),
                        state = pullToRefreshState,
                        isRefreshing = state.isRefreshing
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
                    contentPadding = PaddingValues(
                        start = FluxUI.Space.medium,
                        end = FluxUI.Space.medium,
                        top = headerHeight
                    ),
                    state = gridState,
                ) {

                    if (state.artworks.none { !it.isUnknown }) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CatalogEmptyContent(sendIntent = sendIntent)
                        }
                    }

                    if (state.message.showMessage) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CatalogMessage(sendIntent = sendIntent)
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {

                        CatalogHistory(
                            modifier = Modifier.animateItem(),
                            entries = state.history,
                            sendIntent = sendIntent
                        )

                    }

                    if (state.artworks.any { !it.isUnknown }) {

                        item(span = { GridItemSpan(maxLineSpan) }) {

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
                            ) {

                                CatalogViewMenu(
                                    sortingMode = state.sortingMode,
                                    viewMode = state.viewMode,
                                    sendIntent = sendIntent
                                )

                            }
                        }

                        when (state.viewMode) {
                            CatalogViewMode.GRID -> {
                                catalogViewModeGrid(
                                    artworks = state.artworks,
                                    privateFolderEnabled = state.privateFolderEnabled,
                                    sendIntent = sendIntent
                                )
                            }
                            CatalogViewMode.BY_TYPE -> {
                                catalogViewModeType(
                                    artworks = state.artworks,
                                    sortingMode = state.sortingMode,
                                    privateFolderEnabled = state.privateFolderEnabled,
                                    sendIntent = sendIntent
                                )
                            }
                            CatalogViewMode.BY_GENRE -> {
                                catalogViewModeGenre(
                                    artworks = state.artworks,
                                    genres = state.genres,
                                    sortingMode = state.sortingMode,
                                    privateFolderEnabled = state.privateFolderEnabled,
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
                                artworks = state.artworks,
                                tokenIsMissing = state.tokenIsMissing,
                                privateFolderEnabled = state.privateFolderEnabled,
                                sendIntent = sendIntent
                            )

                        }

                    }

                }

            }

            CatalogHeader(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .onSizeChanged { headerHeight = with(density) { it.height.toDp() } }
                    .blurForeground(state = hazeState, alpha = blurAlpha)
                    .padding(top = paddingValues.calculateTopPadding()),
                sendIntent = sendIntent
            )

        }

        if (state.showSortingSheet) {
            CatalogSortingSheet(
                selectedMode = state.sortingMode,
                sendIntent = sendIntent
            )
        }

        if (state.showViewSheet) {
            CatalogViewModeSheet(
                selectedMode = state.viewMode,
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
                state = CatalogState.Content(
                    artworks = MediaMockups.artworks.toImmutableList(),
                    genres = DetailsMockup.allGenres.toImmutableList(),
                    history = MediaMockups.allMedias.map { it.toHistoryEntry() }.toImmutableList(),
                    isRefreshing = false,
                    tokenIsMissing = false,
                    privateFolderEnabled = true,
                    message = CatalogMessageState(
                        showMessage = true
                    ),
                    sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                    showSortingSheet = false,
                    viewMode = CatalogViewMode.BY_TYPE,
                    showViewSheet = false,
                ),
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
                state = CatalogState.Content(
                    artworks = listOf(MediaMockups.unknownArtwork).toImmutableList(),
                    genres = persistentListOf(),
                    history = persistentListOf(),
                    isRefreshing = false,
                    tokenIsMissing = true,
                    privateFolderEnabled = true,
                    message = CatalogMessageState(
                        showMessage = true
                    ),
                    sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                    showSortingSheet = false,
                    viewMode = CatalogViewMode.BY_TYPE,
                    showViewSheet = false,
                ),
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
                state = CatalogState.Content(
                    artworks = persistentListOf(),
                    genres = persistentListOf(),
                    history = persistentListOf(),
                    isRefreshing = false,
                    tokenIsMissing = true,
                    privateFolderEnabled = true,
                    message = CatalogMessageState(
                        showMessage = true
                    ),
                    sortingMode = CatalogSortingMode.LAST_MODIFICATION,
                    showSortingSheet = false,
                    viewMode = CatalogViewMode.BY_TYPE,
                    showViewSheet = false,
                ),
                sendIntent = {}
            )
        }
    }
}