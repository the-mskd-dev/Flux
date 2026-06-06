package com.mskd.flux.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.data.repository.customization.LocalCustomization
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.navigation.Route
import com.mskd.flux.navigation.Route.Artwork
import com.mskd.flux.navigation.Route.Show
import com.mskd.flux.ui.component.global.FluxScaffold
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.AppThemePreview
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.itemWidthFor
import com.mskd.flux.utils.rememberScreenDimensions

@Composable
fun SearchScreen(
    contentType: ContentType? = null,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
        creationCallback = { factory -> factory.create(contentType) }
    )
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SearchEvent.BackToPreviousScreen -> onBack()
                is SearchEvent.NavigateToMovie -> navigate(Artwork(artworkId = event.artworkId, rgb = event.rgb))
                is SearchEvent.NavigateToShow -> navigate(Show(artworkId = event.artworkId, rgb = event.rgb))
            }
        }
    }

    SearchContent(
        state = state,
        sendIntent = viewModel::handleIntent
    )

}

@Composable
fun SearchContent(
    state: SearchUIState,
    sendIntent: (SearchIntent) -> Unit,
) {

    val focusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    val screenDimensions = rememberScreenDimensions()
    val isLargeScreen = screenDimensions.isLarge
    val columns = if (isLargeScreen) 5 else LocalCustomization.current.itemsPerRow
    val itemWidth = itemWidthFor(columns = columns)

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

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            columns = GridCells.Fixed(columns),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
            contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM)
        ) {

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = state.searchWord,
                    onValueChange = { sendIntent(SearchIntent.DoSearch(it)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.small,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    placeholder = { Text(stringResource(R.string.enter_search)) },
                    trailingIcon = {
                        if (state.searchWord.isNotEmpty()) {
                            IconButton(
                                modifier = Modifier.size(18.dp),
                                onClick = { sendIntent(SearchIntent.DoSearch("")) },
                                content = { Icon(imageVector = Icons.Rounded.Clear, contentDescription = "clear button") }
                            )
                        }
                    }
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                SearchTypeFilters(
                    selectedType = state.contentType,
                    sendIntent = sendIntent
                )

            }

            items(
                items = state.filteredArtworks,
                key = { it.id }
            ) { artwork ->

                Box(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    MediaItem(
                        modifier = Modifier
                            .width(itemWidth)
                            .aspectRatio(Ui.Dimension.ITEM_RATIO),
                        path = artwork.imagePath,
                        hd = false,
                        description = artwork.title,
                        onTap = { sendIntent(SearchIntent.OnArtworkTap(artwork = artwork, rgb = it)) }
                    )

                }


            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
            }

        }

    }

}

@Composable
fun SearchTypeFilters(
    selectedType: ContentType?,
    sendIntent: (SearchIntent) -> Unit
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        FilterChip(
            onClick = { sendIntent(SearchIntent.FilterOnType(ContentType.MOVIE)) },
            label = {
                Text.Label.Medium(
                    text = stringResource(id = R.string.movies).uppercase(),
                )
            },
            selected = selectedType == ContentType.MOVIE,
            leadingIcon = if (selectedType == ContentType.MOVIE) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Movies selected",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { null },
        )

        FilterChip(
            onClick = { sendIntent(SearchIntent.FilterOnType(ContentType.SHOW)) },
            label = {
                Text.Label.Medium(
                    text = stringResource(id = R.string.shows).uppercase(),
                )
            },
            selected = selectedType == ContentType.SHOW,
            leadingIcon = if (selectedType == ContentType.SHOW) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Shows selected",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else { null },
        )

    }
}

@FluxPreview
@Composable
fun SearchContent_Preview() {
    AppThemePreview {
        SearchContent(
            state = SearchUIState(
                searchWord = "",
                artworks = MediaMockups.artworks
            ),
            sendIntent = {}
        )
    }
}