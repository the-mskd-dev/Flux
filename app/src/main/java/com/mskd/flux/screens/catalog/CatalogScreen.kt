package com.mskd.flux.screens.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.features.catalog.presentation.CatalogEvent
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.catalog.presentation.CatalogState
import com.mskd.flux.features.catalog.presentation.CatalogViewModel
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.Route
import com.mskd.flux.screens.catalog.composable.CatalogCategory
import com.mskd.flux.screens.catalog.composable.CatalogGenericItems
import com.mskd.flux.screens.catalog.composable.CatalogTopButtons
import com.mskd.flux.screens.catalog.composable.LastWatchedCarousel
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.FluxSnackbar
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.FluxPreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.empty_catalog
import flux.shared.generated.resources.empty_catalog_desc
import flux.shared.generated.resources.how_to_name_files
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.shows
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is CatalogEvent.NavigateToCategory -> navigate(Route.Search(contentType = event.category))
                is CatalogEvent.NavigateToMovie -> navigate(Route.Artwork(artworkId = event.artworkId, season = null, rgb = event.rgb))
                is CatalogEvent.NavigateToShow -> navigate(Route.Show(artworkId = event.artworkId, rgb = event.rgb))
                CatalogEvent.NavigateToUnknown -> navigate(Route.UnknownArtworks)
                CatalogEvent.NavigateToHowTo -> navigate(Route.HowTo)
                CatalogEvent.NavigateToSearch -> navigate(Route.Search())
                CatalogEvent.NavigateToSettings -> navigate(Route.Settings)
                CatalogEvent.NavigateToToken -> navigate(Route.Token(fromSetup = false))
                CatalogEvent.NavigateToSources -> navigate(Route.Sources(fromSetup = false))
            }
        }
    }

    FluxSnackbar(
        snackbarState = uiState.snackbarState,
        snackbarHostState = snackbarHostState,
        duration = SnackbarDuration.Indefinite,
        withDismissAction = true,
        onDismiss = { viewModel.handleIntent(CatalogIntent.OnDismissSnackbar) },
        onAction = { viewModel.handleIntent(CatalogIntent.OnSnackbarActionTap) }
    )

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
                    lastWatchedIds = state.lastWatchedMediaIds,
                    isRefreshing = state.isRefreshing,
                    snackbarHostState = snackbarHostState,
                    sendIntent = viewModel::handleIntent
                )

            }

            else -> {}
        }

    }

}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CatalogContent(
    artworks: List<Artwork>,
    lastWatchedIds: List<Long>,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    sendIntent: (CatalogIntent) -> Unit
) {

    val pullToRefreshState = rememberPullToRefreshState()
    var offsetY by remember { mutableFloatStateOf(0f) }
    val loaderAnim by animateFloatAsState(pullToRefreshState.distanceFraction.coerceIn(0f, 1f))
    with(LocalDensity.current) {
        offsetY = 100.dp.toPx() * pullToRefreshState.distanceFraction
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState,)
        }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding()))

            CatalogTopButtons(sendIntent = sendIntent)

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

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationY = offsetY },
                    verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
                ) {

                    if (artworks.none { !it.isUnknown }) {

                        item {

                            Text.Headline.Medium(
                                modifier = Modifier
                                    .padding(top = FluxUI.Space.medium)
                                    .padding(horizontal = FluxUI.Space.medium),
                                text = stringResource(Res.string.empty_catalog)
                            )

                        }

                        item {

                            Text.Body.Large(
                                modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                                text = stringResource(Res.string.empty_catalog_desc)
                            )

                        }

                        item {

                            Text.Label.Large(
                                modifier = Modifier
                                    .padding(all = FluxUI.Space.medium)
                                    .clickable { sendIntent(CatalogIntent.OnHowToTap) },
                                text = stringResource(Res.string.how_to_name_files),
                                color = MaterialTheme.colorScheme.primary
                            )

                        }

                    }

                    if (artworks.any { !it.isUnknown }) {

                        item {
                            LastWatchedCarousel(
                                artworks = lastWatchedIds.mapNotNull { artworks.find { o -> o.id == it } },
                                sendIntent = sendIntent
                            )
                        }

                        item {
                            CatalogCategory(
                                name = stringResource(Res.string.shows),
                                category = ContentType.SHOW,
                                artworks = artworks.filter { it.type == ContentType.SHOW && !it.isUnknown },
                                sendIntent = sendIntent
                            )
                        }

                        item {
                            CatalogCategory(
                                name = stringResource(Res.string.movies),
                                category = ContentType.MOVIE,
                                artworks = artworks.filter { it.type == ContentType.MOVIE && !it.isUnknown },
                                sendIntent = sendIntent
                            )
                        }

                    }

                    item {
                        CatalogGenericItems(
                            showUnknown = artworks.any { it.isUnknown },
                            sendIntent = sendIntent
                        )
                    }

                    item {

                        Spacer(
                            modifier = Modifier
                                .height(paddingValues.calculateBottomPadding() + FluxUI.Space.large)
                        )

                    }

                }

            }

        }

    }


}

@FluxPreview
@Composable
fun CatalogScreen_Preview() {
    AppThemePreview {
        Surface {
            CatalogContent(
                artworks = MediaMockups.artworks,
                lastWatchedIds = MediaMockups.artworks.map { it.id },
                isRefreshing = false,
                snackbarHostState = SnackbarHostState(),
                sendIntent = {}
            )
        }
    }
}

@FluxPreview
@Composable
fun CatalogScreen_Empty_Preview() {
    AppThemePreview {
        Surface {
            CatalogContent(
                artworks = listOf(MediaMockups.unknownArtwork),
                lastWatchedIds = emptyList(),
                isRefreshing = false,
                snackbarHostState = SnackbarHostState(),
                sendIntent = {}
            )
        }
    }
}