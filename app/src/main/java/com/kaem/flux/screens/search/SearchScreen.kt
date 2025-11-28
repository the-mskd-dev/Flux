package com.kaem.flux.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Clear
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.navigation.Navigation
import com.kaem.flux.navigation.Route
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.component.MediaItem
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun SearchScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
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

    FluxScaffold(
        title = stringResource(android.R.string.search_go),
        onBackTap = { viewModel.handleIntent(SearchIntent.OnBackTap) }
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
                    onValueChange = { viewModel.handleIntent(SearchIntent.DoSearch(it)) },
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
                                onClick = { viewModel.handleIntent(SearchIntent.DoSearch("")) },
                                content = { Icon(imageVector = Icons.Rounded.Clear, contentDescription = "clear button") }
                            )
                        }
                    }
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
                        onTap = { viewModel.handleIntent(SearchIntent.OnMediaTap(overview.id)) }
                    )

                }


            }

            item(span = { GridItemSpan(3) }) {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
            }

        }

    }

}