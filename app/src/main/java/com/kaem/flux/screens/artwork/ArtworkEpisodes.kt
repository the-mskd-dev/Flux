package com.kaem.flux.screens.artwork

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Status
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.component.SmallText
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants

@Composable
fun ArtworkSeasonsTabs(
    selectedSeason: Int,
    seasons: List<Int>,
    onSeasonTap: (Int) -> Unit
) {

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        items(items = seasons.sorted(), key = { it }) { season ->

            val isSelected = selectedSeason == season
            val backgroundColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.background, label = "seasonTabBackgroundColor")
            val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary, label = "seasonTabTextColor")

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor
                ),
                shape = Ui.Shape.RoundedCorner,
                onClick = { onSeasonTap(season) }
            ) {
                SmallText(
                    text = stringResource(id = R.string.season, season),
                    color = textColor
                )
            }

        }

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun EpisodeItem(
    modifier: Modifier = Modifier,
    episode: Episode,
    isFirst: Boolean,
    onEpisodeTap: () -> Unit
) {

    var alpha by remember { mutableFloatStateOf(if (episode.status == Status.WATCHED) .4f else 1f) }
    val alphaAnimation by animateFloatAsState(targetValue = alpha, label = "alphaAnimation")
    LaunchedEffect(episode.status) {
        alpha = if (episode.status == Status.WATCHED) .4f else 1f
    }

    ConstraintLayout(
        modifier = modifier
            .clickable { onEpisodeTap() }
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = Ui.Space.MEDIUM)
            .padding(bottom = Ui.Space.MEDIUM)
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
                .alpha(alphaAnimation)
                .clip(Ui.Shape.RoundedCorner)
                .aspectRatio(16f / 9f)
                .constrainAs(image) {
                    top.linkTo(divider.bottom, Ui.Space.MEDIUM)
                    start.linkTo(parent.start)
                    end.linkTo(startGuideline)
                    width = Dimension.fillToConstraints
                },
            model = Constants.TMDB.IMAGE + episode.imagePath,
            contentScale = ContentScale.Crop,
            loading = Placeholders.loading(),
            failure = Placeholders.failure(),
            contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
        )

        Column(
            modifier = Modifier
                .alpha(alphaAnimation)
                .constrainAs(content) {
                    top.linkTo(image.top)
                    start.linkTo(startGuideline, Ui.Space.MEDIUM)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            SmallText(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(.8f),
                text = stringResource(R.string.episode, episode.number),
                textAlign = TextAlign.Start,
                fontStyle = FontStyle.Italic
            )

            MediumText(
                modifier = Modifier.fillMaxWidth(),
                text = episode.title,
                textAlign = TextAlign.Start,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

    }

}
