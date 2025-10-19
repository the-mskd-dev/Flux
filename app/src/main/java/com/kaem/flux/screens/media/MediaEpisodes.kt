package com.kaem.flux.screens.media

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.R
import com.kaem.flux.mockups.MediaMockups
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.Status
import com.kaem.flux.ui.component.BoldText
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.component.Placeholders
import com.kaem.flux.ui.component.ProgressBar
import com.kaem.flux.ui.component.SmallText
import com.kaem.flux.ui.theme.Ui
import com.kaem.flux.utils.Constants
import com.kaem.flux.utils.extensions.grayScale

@Composable
fun MediaSeasonsTabs(
    selectedSeason: Int,
    seasons: List<Int>,
    onSeasonTap: (Int) -> Unit
) {

    LazyRow(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = Ui.Space.MEDIUM)
            .fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
    ) {

        items(items = seasons.sorted(), key = { it }) { season ->

            val isSelected = selectedSeason == season
            val backgroundColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer, label = "seasonTabBackgroundColor")
            val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.primary, label = "seasonTabTextColor")

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = backgroundColor
                ),
                shape = Ui.Shape.RoundedCorner,
                onClick = { onSeasonTap(season) }
            ) {
                SmallText(
                    text = stringResource(id = R.string.season, season).uppercase(),
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
    onEpisodeTap: () -> Unit
) {

    val episodeModifier = if (episode.status == Status.WATCHED)
        modifier
            .alpha(.4f)
            .grayScale()
    else
        modifier

    ConstraintLayout(
        modifier = episodeModifier
            .clickable { onEpisodeTap() }
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = Ui.Space.MEDIUM)
    ) {

        val (image, content) = createRefs()
        val startGuideline = createGuidelineFromStart(.3f)

        Box(
            modifier = Modifier
                .clip(Ui.Shape.RoundedCorner)
                .aspectRatio(16f / 9f)
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(startGuideline)
                    width = Dimension.fillToConstraints
                },
            contentAlignment = Alignment.BottomCenter,
            content = {

                GlideImage(
                    modifier = Modifier.fillMaxSize(),
                    model = Constants.TMDB.IMAGE + episode.imagePath,
                    contentScale = ContentScale.Crop,
                    loading = Placeholders.loading(),
                    failure = Placeholders.failure(),
                    contentDescription = "Season ${episode.season} episode ${episode.number}, ${episode.title}"
                )

                if (episode.status == Status.IS_WATCHING) {
                    ProgressBar(
                        modifier = Modifier.fillMaxWidth(),
                        media = episode
                    )
                }

            }
        )

        Column(
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(image.top)
                    start.linkTo(startGuideline, Ui.Space.MEDIUM)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalAlignment = Alignment.Start
        ) {

            BoldText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.episode, episode.number).uppercase(),
                textAlign = TextAlign.Start,
                fontSize = Ui.FontSize.SMALL,
                color = MaterialTheme.colorScheme.primary
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

@Preview(showBackground = true)
@Composable
fun EpisodeItem_Preview() {
    EpisodeItem(
        episode = MediaMockups.episode1,
        onEpisodeTap = {}
    )
}