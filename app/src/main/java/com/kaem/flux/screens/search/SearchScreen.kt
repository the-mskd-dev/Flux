package com.kaem.flux.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.navigation.Route
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.component.MediaItem
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.AppTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun SearchScreen(
    contentType: ContentType? = null,
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
        creationCallback = { factory -> factory.create(contentType) }
    )
) {

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is SearchEvent.NavigateToMedia -> navigate(Route.Media(mediaId = event.mediaId))
                SearchEvent.BackToPreviousScreen -> onBack()
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

    FluxScaffold(
        title = stringResource(android.R.string.search_go),
        onBackTap = { sendIntent(SearchIntent.OnBackTap) }
    ) { innerPadding ->

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
            contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM)
        ) {

            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }

            item(span = { GridItemSpan(3) }) {

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.searchWord,
                    onValueChange = { sendIntent(SearchIntent.DoSearch(it)) },
                    singleLine = true,
                    shape = Ui.Shape.Corner.Small,
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

            item(span = { GridItemSpan(3) }) {

                SearchTypeFilters(
                    selectedType = state.contentType,
                    sendIntent = sendIntent
                )

            }

            items(
                items = state.filteredOverviews,
                key = { it.id }
            ) { overview ->

                BoxWithConstraints(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    MediaItem(
                        width = maxWidth,
                        url = Constants.TMDB.IMAGE_SMALL + overview.imagePath,
                        ratio = 2f/3f,
                        description = overview.title,
                        onTap = { sendIntent(SearchIntent.OnMediaTap(overview.id)) }
                    )

                }


            }

            item(span = { GridItemSpan(3) }) {
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

@Preview
@Composable
fun SearchContent_Preview() {
    AppTheme {
        SearchContent(
            state = SearchUIState(
                searchWord = "preview",
                overviews = MediaMockups.overviews
            ),
            sendIntent = {}
        )
    }
}