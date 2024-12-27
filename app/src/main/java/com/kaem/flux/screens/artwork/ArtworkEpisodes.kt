package com.kaem.flux.screens.artwork

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.utils.Constants

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
            loading = Placeholders.loading,
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
