package com.kaem.flux.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.MediaItem
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun CategoryScreen(
    onBackButtonTap: () -> Unit,
    navigateToDetails: (Long) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {

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

            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {

                BackButton(onTap = onBackButtonTap)

                Text.Headline.Small(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(viewModel.contentType.stringResource)
                )

            }

        }

        items(items = viewModel.overviews) { overview ->

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                MediaItem(
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