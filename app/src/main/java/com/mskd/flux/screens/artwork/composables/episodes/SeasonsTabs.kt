@file:JvmName("SeasonsTabsKt")

package com.mskd.flux.screens.artwork.composables.episodes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.extensions.tmdbImage
import kotlinx.coroutines.launch

@Composable
fun SeasonsTabs(
    seasons: List<Season>,
    currentSeason: Int,
    sendIntent: (ArtworkIntent) -> Unit
) {

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
    ) {

        items(items = seasons) { season ->

            SeasonItem(
                season = season,
                isSelected = season.season == currentSeason,
                sendIntent = sendIntent
            )

        }

    }

}

@Composable
fun SeasonItem(
    season: Season,
    isSelected: Boolean,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val url = season.imagePath.orEmpty().tmdbImage
    val alpha by animateFloatAsState(if (isSelected) 1f else .2f)
    var imageHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .alpha(alpha)
            .width(130.dp)
            .aspectRatio(3f/4f)
            .clip(MaterialTheme.shapes.large)
            .clickable { sendIntent(ArtworkIntent.SelectSeason(season.season)) }
            .onSizeChanged { imageHeight = it.height },
        contentAlignment = Alignment.BottomCenter
    ) {

        Image(
            modifier = Modifier.fillMaxSize(),
            url = url,
            contentDescription = season.title
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = .6f),
                            MaterialTheme.colorScheme.background.copy(alpha = .9f),
                            MaterialTheme.colorScheme.background,
                        ),
                        startY = imageHeight * .7f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

        Text.Title.Large(
            modifier = Modifier.padding(bottom = Ui.Space.MEDIUM),
            text = stringResource(R.string.season, season.season),
            emphasized = true
        )

    }

}

@FluxPreview
@Composable
fun SeasonsTabs_Preview() {
    AppTheme {
        SeasonsTabs(
            seasons = MediaMockups.seasons,
            currentSeason = 1,
            sendIntent = {}
        )
    }
}