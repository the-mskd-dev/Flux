package com.kaem.flux.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.model.flux.ArtworkContent
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxStatus
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxElevation
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.ui.theme.FluxWeight
import com.kaem.flux.utils.Constants
import java.text.DateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    onBackButtonTap: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        item {

            Column(
                modifier = Modifier.padding(bottom = FluxSpace.MEDIUM),
                verticalArrangement = Arrangement.spacedBy(FluxSpace.LARGE)
            ) {

                DetailsHeader(
                    uiState = uiState,
                    onBackButtonTap = { onBackButtonTap() },
                    onLaunchButtonTap = {}
                )

                DetailsDescription(uiState = uiState)

            }

        }

        if (uiState.artwork.content is ArtworkContent.SHOW) {

            val episodes = (uiState.artwork.content as ArtworkContent.SHOW).episodes

            item {

                var isExpanded by remember { mutableStateOf(false) }

                DetailsSeasonsDropDown(
                    isExpanded = isExpanded,
                    selectedSeason = uiState.currentSeason,
                    seasons = episodes.map { it.season }.distinct(),
                    onSeasonTap = { viewModel.selectSeason(it); isExpanded = false},
                    onExpandedChange = { isExpanded = it }
                )

            }

            items(items = episodes.filter { it.season == uiState.currentSeason }.sortedBy { it.number }, key = { it.id }) { episode ->

                DetailsEpisode(
                    episode = episode,
                    onWatchTap = {},
                    isExpanded = uiState.expandedEpisodeId == episode.id,
                    expandDetails = { viewModel.expandEpisodeDetails(episode.id) },
                    onWatchStatusChange = { viewModel.changeWatchStatus(episode) }
                )

            }

        }

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailsHeader(
    uiState: DetailsUiState,
    onBackButtonTap: () -> Unit,
    onLaunchButtonTap: () -> Unit
) {

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {

        val (image, back, title, watchButton, checkButton) = createRefs()

        GlideImage(
            modifier = Modifier
                .aspectRatio(6f / 5f)
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            model = Constants.TMDB.IMAGE + uiState.artwork.bannerPath,
            contentScale = ContentScale.Crop,
            contentDescription = uiState.artwork.title
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
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = CircleShape
                )
                .background(color = MaterialTheme.colorScheme.background)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                tint = MaterialTheme.colorScheme.onBackground,
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

                val textId = if (uiState.artworkDetails?.status == FluxStatus.IS_WATCHING) R.string.resume else R.string.start
                Text(
                    text = stringResource(id = textId).uppercase(),
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
            onClick = {  },
            content = { Icon(imageVector = Icons.Rounded.Done, contentDescription = "check if watched button") }
        )

        DetailsTitle(
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
fun DetailsTitle(
    modifier: Modifier,
    uiState: DetailsUiState
) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        Title(
            modifier = Modifier.fillMaxWidth(),
            text = uiState.artwork.title
        )

        uiState.currentEpisode?.releaseDate ?: (uiState.artwork.content as? ArtworkContent.MOVIE)?.movie?.releaseDate?.let {
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
fun DetailsDescription(uiState: DetailsUiState) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FluxSpace.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.SMALL)
    ) {

        uiState.currentEpisode?.let {

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

        uiState.artwork.description?.let {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(.8f),
                text = it,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.MEDIUM,
                textAlign = TextAlign.Start
            )
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsSeasonsDropDown(
    isExpanded: Boolean,
    selectedSeason: Int,
    seasons: List<Int>,
    onSeasonTap: (Int) -> Unit,
    onExpandedChange: (Boolean) -> Unit
) {

    ExposedDropdownMenuBox(
        modifier = Modifier.padding(FluxSpace.MEDIUM),
        expanded = isExpanded,
        onExpandedChange = { onExpandedChange(it) }
    ) {

        TextField(
            value = stringResource(id = R.string.season, selectedSeason),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            placeholder = { Text(text = stringResource(id = R.string.select_season)) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {

            seasons.sorted().forEach {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.season, it)) },
                    onClick = {
                        onSeasonTap(it)
                    }
                )
            }

        }

    }

}

@Composable
fun DetailsEpisode(
    episode: Episode,
    isExpanded: Boolean,
    expandDetails: () -> Unit,
    onWatchTap: () -> Unit,
    onWatchStatusChange: () -> Unit
) {

    var isWatched by remember { mutableStateOf(episode.status == FluxStatus.WATCHED) }
    val alphaAnimation by animateFloatAsState(targetValue = if (isWatched) .4f else 1f, label = "alphaAnimation")
    val colorAnimation by animateColorAsState(targetValue = if (isWatched) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary, label = "colorAnimation")

    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = FluxSpace.MEDIUM)
            .padding(bottom = FluxSpace.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(.2f),
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) { expandDetails() }
                .alpha(alphaAnimation)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FluxSpace.SMALL),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier.alpha(.8f),
                text = "${episode.number}",
                color = colorAnimation,
                fontSize = FluxFontSize.MEDIUM
            )

            Text(
                text = episode.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.MEDIUM
            )

        }

        AnimatedVisibility(visible = isExpanded) {
            DetailsEpisodeContent(
                episode = episode,
                onCloseExpand = { expandDetails() },
                onWatchTap = onWatchTap,
                onWatchStatusChange = {
                    onWatchStatusChange()
                    isWatched = episode.status == FluxStatus.WATCHED
                }
            )
        }

    }

}

@Composable
fun DetailsEpisodeContent(
    episode: Episode,
    onCloseExpand: () -> Unit,
    onWatchTap: () -> Unit,
    onWatchStatusChange: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FluxSpace.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        Text(
            modifier = Modifier
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) { onCloseExpand() }
                .fillMaxWidth()
                .alpha(.8f),
            text = episode.description,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = FluxFontSize.MEDIUM,
            textAlign = TextAlign.Start
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier.clickable { onWatchStatusChange() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FluxWeight.MEDIUM,
                text = stringResource(id = if (episode.status == FluxStatus.WATCHED) R.string.mark_as_not_watched else R.string.mark_as_watched).uppercase()
            )

            Text(
                modifier = Modifier.clickable { onWatchTap() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FluxWeight.MEDIUM,
                text = stringResource(id = if (episode.status == FluxStatus.IS_WATCHING) R.string.resume else R.string.start).uppercase()
            )

        }

    }

}

