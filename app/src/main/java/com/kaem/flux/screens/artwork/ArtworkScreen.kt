package com.kaem.flux.screens.artwork

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.kaem.flux.R

import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.screens.player.PlayerScreen
import com.kaem.flux.ui.component.Loader
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxElevation
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.timeDescription
import kotlinx.coroutines.launch
import java.text.DateFormat

@Composable
fun ArtworkScreen(
    onBackButtonTap: () -> Unit,
    viewModel: ArtworkViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp),
        state = scrollState
    ) {

        item {

            Column(
                modifier = Modifier.padding(bottom = FluxSpace.MEDIUM),
                verticalArrangement = Arrangement.spacedBy(FluxSpace.LARGE)
            ) {

                ArtworkHeader(
                    uiState = uiState,
                    onBackButtonTap = { onBackButtonTap() },
                    onStatusButtonTap = { viewModel.changeWatchStatus() },
                    onLaunchButtonTap = { viewModel.showPlayer(true) }
                )

                ArtworkDescription(uiState = uiState)

            }

        }

        when (val screen = uiState.screen) {
            is ArtworkUiState.Screen.MOVIE -> {}
            ArtworkUiState.Screen.LOADING -> {

                item {
                    Loader()
                }

            }
            ArtworkUiState.Screen.ERROR -> {
                item {
                    Text("Error") //TODO : Error message
                }
            }
            is ArtworkUiState.Screen.SHOW -> {

                val episodes = screen.episodes

                item {

                    ArtworkSeasonsTabs(
                        selectedSeason = uiState.currentSeason,
                        seasons = episodes.map { it.season }.distinct(),
                        onSeasonTap = { viewModel.selectSeason(it) }
                    )

                }

                itemsIndexed(
                    items = episodes
                        .filter { it.season == uiState.currentSeason }
                        .sortedBy { it.number },
                    key = { _, e -> e.id }
                ) { i, episode ->

                    EpisodeItem(
                        episode = episode,
                        isFirst = i == 0,
                        onEpisodeTap = {
                            scope.launch {
                                viewModel.selectArtwork(episode)
                                scrollState.animateScrollToItem(0)
                            }
                        }
                    )

                }

            }
        }

    }

    AnimatedVisibility(uiState.showPlayer) {
        PlayerScreen(
            artwork = uiState.selectedArtwork,
            onBackButtonTap = { viewModel.showPlayer(false) },
            onTimeSave = { viewModel.saveTime(it) }
        )
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArtworkHeader(
    uiState: ArtworkUiState,
    onBackButtonTap: () -> Unit,
    onStatusButtonTap: () -> Unit,
    onLaunchButtonTap: () -> Unit
) {

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {

        val (image, back, title, watchButton, checkButton) = createRefs()

        val imagePath = when (uiState.selectedArtwork) {
            is Episode -> uiState.selectedArtwork.imagePath
            else -> uiState.overview.bannerPath
        }

        GlideImage(
            modifier = Modifier
                .aspectRatio(6f / 5f)
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            model = Constants.TMDB.IMAGE + imagePath,
            contentScale = ContentScale.Crop,
            loading = placeholder(ColorPainter(Color.LightGray)),
            contentDescription = uiState.overview.title
        )

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .constrainAs(back) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start, FluxSpace.MEDIUM)
                }
                .size(50.dp)
                .clip(shape = CircleShape)
                .clickable { onBackButtonTap() }
                .padding(FluxSpace.EXTRA_SMALL),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                tint = Color.White,
                contentDescription = "back button"
            )

        }

        Button(
            modifier = Modifier
                .constrainAs(watchButton) {
                    top.linkTo(image.bottom)
                    bottom.linkTo(image.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            onClick = { onLaunchButtonTap() },
            elevation = FluxElevation.buttonElevation(),
            shape = RoundedCornerShape(8.dp)
        ) {

            Row(
                modifier = Modifier.padding(horizontal = FluxSpace.MEDIUM),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FluxSpace.SMALL, Alignment.CenterHorizontally)
            ) {

                Icon(
                    modifier = Modifier.size(30.dp),
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "play button"
                )


                val text = if (uiState.selectedArtwork?.status == Status.IS_WATCHING) stringResource(id = R.string.resume, uiState.selectedArtwork.currentTime.timeDescription) else stringResource(R.string.start)
                Text(
                    text = text.uppercase(),
                    fontWeight = FluxWeight.MEDIUM
                )

            }

        }

        FloatingActionButton(
            modifier = Modifier.constrainAs(checkButton) {
                top.linkTo(watchButton.top)
                bottom.linkTo(watchButton.bottom)
                start.linkTo(watchButton.end, FluxSpace.LARGE)
                height = Dimension.value(40.dp)
                width = Dimension.value(40.dp)
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FluxElevation.floatingButtonElevation(),
            onClick = { onStatusButtonTap() },
            content = { Icon(imageVector = Icons.Rounded.Done, contentDescription = "check if watched button") }
        )

        ArtworkTitle(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(watchButton.bottom, FluxSpace.SMALL)
                start.linkTo(parent.start, FluxSpace.MEDIUM)
                end.linkTo(parent.end, FluxSpace.MEDIUM)
                width = Dimension.fillToConstraints
            },
            uiState = uiState
        )

    }

}

@Composable
fun ArtworkTitle(
    modifier: Modifier,
    uiState: ArtworkUiState
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        Title(
            modifier = Modifier.fillMaxWidth(),
            text = uiState.overview.title
        )

        uiState.selectedArtwork?.releaseDate?.let {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(.8f),
                text = DateFormat.getDateInstance().format(it),
                fontSize = FluxFontSize.SMALL,
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = FontStyle.Italic
            )
        }

    }

}

@Composable
fun ArtworkDescription(uiState: ArtworkUiState) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FluxSpace.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.SMALL)
    ) {

        uiState.selectedArtwork?.let {

            if (it is Episode) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.season_and_episode, it.season, it.number),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = FluxFontSize.LARGE,
                    fontWeight = FluxWeight.MEDIUM
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = FluxFontSize.MEDIUM,
                    fontWeight = FluxWeight.MEDIUM
                )

            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(.8f),
                text = it.description,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.MEDIUM,
                textAlign = TextAlign.Start
            )

        }

    }

}

@Composable
fun ArtworkSeasonsTabs(
    selectedSeason: Int,
    seasons: List<Int>,
    onSeasonTap: (Int) -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth()) {

        ScrollableTabRow(selectedTabIndex = seasons.indexOf(selectedSeason)) {

            seasons.sorted().forEach { season ->

                Tab(
                    text = { Text(text = stringResource(id = R.string.season, season)) },
                    selected = selectedSeason == season,
                    onClick = { onSeasonTap(season) }
                )

            }

        }
    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EpisodeItem(
    episode: Episode,
    isFirst: Boolean,
    onEpisodeTap: () -> Unit
) {

    var alpha by remember { mutableFloatStateOf(1f) }
    val alphaAnimation by animateFloatAsState(targetValue = alpha, label = "alphaAnimation")
    LaunchedEffect(episode.status) {
        alpha = if (episode.status == Status.WATCHED) .4f else 1f
    }

    ConstraintLayout(
        modifier = Modifier
            .clickable { onEpisodeTap() }
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = FluxSpace.MEDIUM)
            .padding(bottom = FluxSpace.MEDIUM)
            .alpha(alphaAnimation)
    ) {

        val (divider, image, content) = createRefs()
        val startGuideline = createGuidelineFromStart(.3f)

        HorizontalDivider(
            modifier = Modifier
                .constrainAs(divider) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .alpha(if (isFirst) 0f else .2f),
            color = MaterialTheme.colorScheme.onBackground
        )

        GlideImage(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .aspectRatio(16f / 9f)
                .constrainAs(image) {
                    top.linkTo(divider.bottom, FluxSpace.MEDIUM)
                    start.linkTo(parent.start)
                    end.linkTo(startGuideline)
                    width = Dimension.fillToConstraints
                },
            model = Constants.TMDB.IMAGE + episode.imagePath,
            contentScale = ContentScale.Crop,
            loading = placeholder(ColorPainter(Color.LightGray)),
            contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
        )

        Column(
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(image.top)
                    start.linkTo(startGuideline, FluxSpace.MEDIUM)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(FluxSpace.EXTRA_SMALL)
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(.8f),
                text = stringResource(R.string.episode, episode.number),
                fontSize = FluxFontSize.SMALL,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = FontStyle.Italic
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = episode.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.MEDIUM,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

    }

}
