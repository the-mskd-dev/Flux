package com.kaem.flux.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaem.flux.R
import com.kaem.flux.screens.home.ArtworkItem
import com.kaem.flux.ui.component.FluxTopBar
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun SearchScreen(
    onBackButtonTap: () -> Unit,
    navigateToDetails: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsState()

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

            FluxTopBar(
                text = stringResource(android.R.string.search_go),
                onBackButtonTap = onBackButtonTap
            )

        }

        item(span = { GridItemSpan(3) }) {

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.searchWord,
                onValueChange = { viewModel.updateSearchWord(it) },
                singleLine = true,
                shape = Ui.Shape.RoundedCorner,
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
                            onClick = { viewModel.updateSearchWord("") },
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

                ArtworkItem(
                    width = maxWidth,
                    url = Constants.TMDB.IMAGE_SMALL + overview.imagePath,
                    ratio = 2f/3f,
                    description = overview.title,
                    onTap = { navigateToDetails(overview.id) }
                )

            }


        }

        item(span = { GridItemSpan(3) }) {
            Box(modifier = Modifier.navigationBarsPadding())
        }

    }

}