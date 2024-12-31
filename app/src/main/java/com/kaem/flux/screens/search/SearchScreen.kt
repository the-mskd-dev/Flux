package com.kaem.flux.screens.search

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaem.flux.screens.home.ArtworkItem
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight
import com.kaem.flux.utils.Constants

@Composable
fun SearchScreen(
    onBackButtonTap: () -> Unit
) {

    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(FluxSpace.SMALL),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.SMALL),
        contentPadding = PaddingValues(horizontal = FluxSpace.MEDIUM)
    ) {

        item(span = { GridItemSpan(3) }) {

            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {

                BackButton(onTap = onBackButtonTap)

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(android.R.string.search_go),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FluxWeight.BOLD,
                    fontSize = FluxFontSize.LARGE,
                )

            }

        }

//        items(items = viewModel.overviews) { overview ->
//
//            BoxWithConstraints(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//
//                ArtworkItem(
//                    width = maxWidth,
//                    url = Constants.TMDB.IMAGE_SMALL + overview.imagePath,
//                    ratio = 2f/3f,
//                    description = overview.title,
//                    onTap = { navigateToDetails(overview.id) }
//                )
//
//            }
//
//
//        }

        item(span = { GridItemSpan(3) }) {
            Box(modifier = Modifier.navigationBarsPadding())
        }

    }

}