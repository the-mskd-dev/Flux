package com.kaem.flux.screens.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.kaem.flux.R
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxStatus
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.utils.Constants
import java.text.DateFormat

@Composable
fun DetailsScreen(
    onBackButtonTap: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState ?: run {
        onBackButtonTap()
        return
    }

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

        if (uiState.episodes.isNotEmpty()) {

            item {

                var isExpanded by remember { mutableStateOf(false) }

                DetailsSeasonsDropDown(
                    isExpanded = isExpanded,
                    selectedSeason = uiState.currentSeason,
                    seasons = uiState.episodes.map { it.season }.distinct(),
                    onSeasonTap = { viewModel.selectSeason(it); isExpanded = false},
                    onExpandedChange = { isExpanded = it }
                )

            }

        }

        items(items = uiState.episodes.filter { it.season == uiState.currentSeason }, key = { it.id }) {
            DetailsEpisode(
                episode = it,
                onWatchTap = {},
                onIsWatchedTap = {}
            )
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

        FloatingActionButton(
            modifier = Modifier.constrainAs(watchButton) {
                top.linkTo(image.bottom)
                bottom.linkTo(image.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.value(70.dp)
                width = Dimension.value(70.dp)
            },
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = { onLaunchButtonTap() },
            content = {
                Icon(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "play button"
                )
            }
        )

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

        uiState.currentEpisode?.releaseDate ?: uiState.artwork.releaseDate?.let {
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
                fontSize = FluxFontSize.LARGE
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it.title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.MEDIUM
            )

        }

        uiState.description?.let {
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailsEpisode(
    episode: FluxEpisode,
    onWatchTap: () -> Unit,
    onIsWatchedTap: () -> Unit
) {

    val isWatched = episode.status == FluxStatus.WATCHED
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = FluxSpace.MEDIUM)
            .padding(bottom = FluxSpace.SMALL),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.SMALL)
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
                ) { isExpanded = !isExpanded }
                .alpha(if (isWatched) .4f else 1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM),
            verticalAlignment = Alignment.CenterVertically
        ) {

            GlideImage(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .height(70.dp)
                    .aspectRatio(3f / 2f),
                model = Constants.TMDB.IMAGE_SMALL + episode.imagePath,
                contentDescription = episode.title,
                loading = placeholder(ColorPainter(Color.LightGray))
            )

            Column {
                Text(
                    modifier = Modifier.alpha(.8f),
                    text = stringResource(id = R.string.episode, episode.number),
                    color = if (isWatched) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.primary,
                    fontSize = FluxFontSize.SMALL
                )
                Text(
                    text = episode.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = FluxFontSize.MEDIUM
                )
            }

        }

        AnimatedVisibility(visible = isExpanded) {
            DetailsEpisodeContent(
                episode = episode,
                onCloseExpand = { isExpanded = false },
                onWatchTap = onWatchTap,
                onIsWatchedTap = onIsWatchedTap
            )
        }

    }

}

@Composable
fun DetailsEpisodeContent(
    episode: FluxEpisode,
    onCloseExpand: () -> Unit,
    onWatchTap: () -> Unit,
    onIsWatchedTap: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FluxSpace.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.MEDIUM)
    ) {

        Text(
            modifier = Modifier
                .clickable(interactionSource = MutableInteractionSource(), indication = null) { onCloseExpand() }
                .fillMaxWidth()
                .alpha(.8f),
            text = episode.description,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = FluxFontSize.MEDIUM,
            textAlign = TextAlign.Start
        )

        Row(
            modifier = Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(FluxSpace.SMALL),
            verticalAlignment = Alignment.CenterVertically
        ) {

            FloatingActionButton(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onWatchTap() },
                content = {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "play button"
                    )
                }
            )

            FloatingActionButton(
                modifier = Modifier.size(30.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onIsWatchedTap() },
                content = { Icon(imageVector = Icons.Rounded.Done, contentDescription = "check if watched button") }
            )

        }


    }

}