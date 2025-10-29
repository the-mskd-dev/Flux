package com.kaem.flux.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.kaem.flux.ui.component.FluxScaffold
import com.kaem.flux.ui.component.MediaItem
import com.kaem.flux.ui.theme.FluxTheme
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun CategoryScreen(
    navigate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.event.collect {
            when(it) {
                is CategoryEvent.NavigateToMedia -> navigate(Navigation.MEDIA.build(listOf(it.mediaId)))
                CategoryEvent.BackToPreviousScreen -> onBack()
            }
        }
    }

    CategoryScreenContent(
        overviews = viewModel.overviews,
        contentType = viewModel.contentType,
        sendIntent = viewModel::handleIntent
    )


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreenContent(
    overviews: List<MediaOverview>,
    contentType: ContentType,
    sendIntent: (CategoryIntent) -> Unit,
) {

    FluxScaffold(
        title = stringResource(contentType.stringResource),
        onBackTap = { sendIntent(CategoryIntent.OnBackTap) }
    ) { innerPadding ->

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
            contentPadding = PaddingValues(all = Ui.Space.MEDIUM)
        ) {

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
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

            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
            }

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