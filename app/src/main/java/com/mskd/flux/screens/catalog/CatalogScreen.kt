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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
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
import com.mskd.flux.screens.catalog.composable.CatalogGenericCategory
import com.mskd.flux.screens.catalog.composable.CatalogTopButtons
import com.mskd.flux.screens.catalog.composable.LastWatchedCarousel
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.add_source
import flux.shared.generated.resources.empty_catalog
import flux.shared.generated.resources.empty_catalog_desc
import flux.shared.generated.resources.how_to_name_files
import flux.shared.generated.resources.ic_add_folder
import flux.shared.generated.resources.ic_api
import flux.shared.generated.resources.ic_flux
import flux.shared.generated.resources.movies
import flux.shared.generated.resources.other_files
import flux.shared.generated.resources.shows
import flux.shared.generated.resources.sync_in_progress
import flux.shared.generated.resources.tmdb_api_token
import org.jetbrains.compose.resources.painterResource
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
                    tokenIsMissing = state.tokenIsMissing,
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
    tokenIsMissing: Boolean,
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

                            Text.Content.Title(
                                modifier = Modifier
                                    .padding(top = FluxUI.Space.medium)
                                    .padding(horizontal = FluxUI.Space.medium),
                                text = stringResource(Res.string.empty_catalog)
                            )

                        }

                        item {

                            Text.Content.Body(
                                modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                                text = stringResource(Res.string.empty_catalog_desc)
                            )

                        }

                        item {

                            TextButton(
                                onClick = { sendIntent(CatalogIntent.OnHowToTap) },
                                contentPadding = PaddingValues(all = FluxUI.Space.medium)
                            ) {
                                Text.Button(text = stringResource(Res.string.how_to_name_files),)
                            }

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

                    if (artworks.any { it.isUnknown }) {
                        item {
                            CatalogGenericCategory(
                                name = stringResource(Res.string.other_files),
                                painter = painterResource(Res.drawable.ic_flux),
                                iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                onTap = { sendIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN)) }
                            )
                        }
                    }

                    if (artworks.isEmpty()) {
                        item {
                            CatalogGenericCategory(
                                name = stringResource(Res.string.add_source),
                                painter = painterResource(Res.drawable.ic_add_folder),
                                iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                                onTap = { sendIntent(CatalogIntent.OnSourcesTap) }
                            )
                        }
                    }

                    if (tokenIsMissing) {
                        item {
                            CatalogGenericCategory(
                                name = stringResource(Res.string.tmdb_api_token),
                                painter = painterResource(Res.drawable.ic_api),
                                iconColor = MaterialTheme.colorScheme.onSurface,
                                backgroundColor = MaterialTheme.colorScheme.surfaceBright,
                                onTap = { sendIntent(CatalogIntent.OnTokenTap) }
                            )
                        }
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
    FluxThemePreview {
        Surface {
            CatalogContent(
                artworks = MediaMockups.artworks,
                lastWatchedIds = MediaMockups.artworks.map { it.id },
                isRefreshing = false,
                tokenIsMissing = false,
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
                lastWatchedIds = emptyList(),
                isRefreshing = false,
                tokenIsMissing = true,
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
                lastWatchedIds = emptyList(),
                isRefreshing = false,
                tokenIsMissing = true,
                sendIntent = {}
            )
        }
    }
}