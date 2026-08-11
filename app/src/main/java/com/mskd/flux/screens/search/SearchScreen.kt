package com.mskd.flux.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.features.search.presentation.SearchEvent
import com.mskd.flux.features.search.presentation.SearchIntent
import com.mskd.flux.features.search.presentation.SearchUIState
import com.mskd.flux.features.search.presentation.SearchViewModel
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.navigation.domain.Route
import com.mskd.flux.screens.search.components.SearchContentGrid
import com.mskd.flux.screens.search.components.SearchFilters
import com.mskd.flux.screens.search.components.SearchGenresSheet
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.FluxSearchField
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.itemWidthFor
import com.mskd.flux.utils.rememberScreenDimensions
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SearchScreen(
    withType: ContentType? = null,
    withGenre: Genre? = null,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(withType, withGenre) })
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SearchEvent.BackToPreviousScreen -> onBack()
                is SearchEvent.NavigateToMovie -> navigate(Route.Artwork(artworkId = event.artworkId, rgb = event.rgb))
                is SearchEvent.NavigateToShow -> navigate(Route.Show(artworkId = event.artworkId, rgb = event.rgb))
            }
        }
    }

    SearchContent(
        state = state,
        sendIntent = viewModel::handleIntent
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchContent(
    state: SearchUIState,
    sendIntent: (SearchIntent) -> Unit,
) {

    val focusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    val screenDimensions = rememberScreenDimensions()
    val isLargeScreen = screenDimensions.isLarge
    val columns = if (isLargeScreen) 5 else FluxUI.itemsPerRow.artworks
    var itemWidth by remember { mutableStateOf(FluxUI.Dimension.itemWidth) }
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (isScrolling) {
                    focusManager.clearFocus()
                }
            }
    }

    LaunchedEffect(Unit) {
        if (state.autoKeyboard && !focusRequested) {
            focusRequested = true
            focusRequester.requestFocus()
        }
    }

    FluxScaffold(
        title = stringResource(android.R.string.search_go),
        onBackTap = { sendIntent(SearchIntent.OnBackTap) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    with(density) {
                        itemWidth = itemWidthFor(
                            screenWidthDp = size.width.toDp(),
                            columns = columns
                        )
                    }
                }
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = FluxUI.Space.medium),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        ) {

            FluxSearchField(
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = state.actions.input,
                onValueChange = { sendIntent(SearchIntent.DoSearch(it)) },
            )

            SearchFilters(
                selectedType = state.actions.selectedType,
                selectedGenresCount = state.actions.selectedGenres.size,
                showGenresFilter = state.availableGenres.isNotEmpty(),
                sendIntent = sendIntent
            )

            SearchContentGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = lazyGridState,
                columns = columns,
                artworks = state.artworks,
                bottomPadding = innerPadding.calculateBottomPadding(),
                itemWidth = itemWidth,
                sendIntent = sendIntent
            )

        }

        if (state.actions.showGenresSelection) {
            SearchGenresSheet(
                genres = state.availableGenres,
                selectedGenreIds = state.actions.selectedGenres,
                sendIntent = sendIntent
            )
        }

    }

}



@FluxPreview
@Composable
fun SearchContent_Preview() {
    FluxThemePreview {
        SearchContent(
            state = SearchUIState(
                artworks = MediaMockups.artworks.toImmutableList(),
                availableGenres = DetailsMockup.allGenres.toImmutableList()
            ),
            sendIntent = {}
        )
    }
}

@FluxPreview
@Composable
fun SearchContent_Empty_Preview() {
    FluxThemePreview {
        SearchContent(
            state = SearchUIState(
                artworks = persistentListOf(),
                availableGenres = DetailsMockup.allGenres.toImmutableList()
            ),
            sendIntent = {}
        )
    }
}