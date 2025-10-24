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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kaem.flux.Navigation.Navigation
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.ContentType
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.ui.component.BackButton
import com.kaem.flux.ui.component.MediaItem
import com.kaem.flux.ui.component.Text
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun CategoryScreen(
    navigate: (String) -> Unit,
    backToPreviousScreen: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.event.collect {
            when(it) {
                is CategoryEvent.NavigateToMedia -> navigate(Navigation.MEDIA.build(listOf(it.mediaId)))
                CategoryEvent.BackToPreviousScreen -> backToPreviousScreen()
            }
        }
    }

    CategoryScreenContent(
        overviews = viewModel.overviews,
        contentType = viewModel.contentType,
        sendIntent = viewModel::handleIntent
    )


}

@Composable
fun CategoryScreenContent(
    overviews: List<MediaOverview>,
    contentType: ContentType,
    sendIntent: (CategoryIntent) -> Unit,
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

                BackButton(onTap = { sendIntent(CategoryIntent.OnBackTap) })

                Text.Headline.Small(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(contentType.stringResource)
                )

            }

        }

        items(items = overviews) { overview ->

            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                MediaItem(
                    width = maxWidth,
                    url = Constants.TMDB.IMAGE_SMALL + overview.imagePath,
                    ratio = 2f/3f,
                    description = overview.title,
                    onTap = { sendIntent(CategoryIntent.OnMediaTap(overview.id)) }
                )

            }


        }

        item(span = { GridItemSpan(3) }) {
            Box(modifier = Modifier.navigationBarsPadding())
        }

    }

}

@Preview
@Composable
fun CategoryScreen_Preview() {
    FluxTheme {
        CategoryScreenContent(
            overviews = MediaMockups.overviews,
            contentType = ContentType.MOVIE,
            sendIntent = {}
        )
    }
}