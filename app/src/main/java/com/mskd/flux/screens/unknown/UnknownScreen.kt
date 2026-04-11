package com.mskd.flux.screens.unknown

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import coil3.video.videoFrameMillis
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.ScreenState
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Status
import com.mskd.flux.navigation.Route
import com.mskd.flux.screens.search.SearchIntent
import com.mskd.flux.screens.search.SearchTypeFilters
import com.mskd.flux.ui.component.BackButton
import com.mskd.flux.ui.component.ErrorScreen
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.component.LoadingScreen
import com.mskd.flux.ui.component.MediaItem
import com.mskd.flux.ui.component.MediaThumbnail
import com.mskd.flux.ui.component.ProgressBar
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.grayScale
import com.mskd.flux.utils.extensions.minToMs
import com.mskd.flux.utils.extensions.timeDescription
import com.mskd.flux.utils.extensions.tmdbImage

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UnknownScreen(
    navigate: (Route) -> Unit,
    onBack: () -> Unit,
    viewModel: UnknownViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                UnknownEvent.BackToPreviousScreen -> onBack()
                is UnknownEvent.PlayMedia -> navigate(Route.Player(mediaId = event.mediaId))
            }
        }
    }

    Crossfade(targetState = uiState.screen) { screen ->

        when (screen) {
            ScreenState.LOADING -> LoadingScreen()
            ScreenState.ERROR -> {
                ErrorScreen(
                    message = stringResource(R.string.oups_an_error_occured),
                    onBackButtonTap = { viewModel.handleIntent(UnknownIntent.OnBackTap) }
                )
            }
            ScreenState.CONTENT -> {
                UnknownScreenContent(
                    medias = uiState.medias,
                    sendIntent = viewModel::handleIntent
                )
            }
        }

    }

}

@Composable
fun UnknownScreenContent(
    medias: List<Episode>,
    sendIntent: (UnknownIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(R.string.my_files),
        onBackTap = { sendIntent(UnknownIntent.OnBackTap) }
    ) { innerPadding ->

        if (medias.isNotEmpty()) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM)
            ) {

                item {
                    Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
                }

                itemsIndexed(items = medias, key = { i, m -> m.id }) { i, media ->

                    if (i != 0) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM))
                    }

                    UnknownItem(
                        media = media,
                        sendIntent = sendIntent
                    )

                }

                item {
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
                }

            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .systemBarsPadding(),
                contentAlignment = Alignment.TopStart
            ) {

                Text.Body.Large(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    text = stringResource(R.string.no_item),
                    textAlign = TextAlign.Center
                )

            }

        }

    }

}

@Composable
fun UnknownItem(
    media: Episode,
    sendIntent: (UnknownIntent) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { sendIntent(UnknownIntent.PlayMedia(media)) }
            .padding(vertical = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL),
    ) {

        MediaThumbnail(
            modifier = Modifier.width(160.dp),
            media = media,
        )

        Column(
            modifier = Modifier.weight(.6f),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL),
            horizontalAlignment = Alignment.Start
        ) {

            Text.Title.Medium(
                text = media.title,
                emphasized = true
            )

            if (media.season > 0 && media.number > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)) {
                    Text.Label.Small(
                        text = stringResource(id = R.string.season, media.season).uppercase(),
                        emphasized = true,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text.Label.Small(
                        text = stringResource(id = R.string.episode, media.number).uppercase(),
                        emphasized = true,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)) {

                Text.Label.Small(
                    text = media.duration.minToMs.timeDescription(),
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (media.status == Status.IS_WATCHING) {
                    val remainingTime = (media.duration.minToMs - media.currentTime).timeDescription(withoutSeconds = true)
                    Text.Label.Small(
                        text = "(" + stringResource(R.string.remaining_time, remainingTime) + ")",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

            }

        }

    }

}

@FluxPreview
@Composable
fun UnknownScreen_Preview() {
    AppTheme {
        UnknownScreenContent(
            medias = MediaMockups.episodesWithStatus,
            sendIntent = {}
        )
    }
}