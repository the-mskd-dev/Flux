package com.mskd.flux.screens.artwork.composables.episodes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.model.artwork.Season
import com.mskd.flux.screens.artwork.ArtworkIntent
import com.mskd.flux.ui.component.Image
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.extensions.tmdbImage
import kotlinx.coroutines.launch

@Composable
fun SeasonsPager(
    seasons: List<Season>,
    currentSeason: Int,
    sendIntent: (ArtworkIntent) -> Unit
) {

    val ratio = 1920f/1080f
    val pagerState = rememberPagerState(initialPage = currentSeason) { seasons.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        sendIntent(ArtworkIntent.SelectSeason(pagerState.currentPage + 1))
    }

    HorizontalPager(
        modifier = Modifier.fillMaxWidth(),
        state = pagerState,
        pageSize = PageSize.Fixed(350.dp),
        pageSpacing = Ui.Space.EXTRA_SMALL,
        contentPadding = PaddingValues(horizontal = Ui.Space.MEDIUM)
    ) { i ->

        val season = seasons[i]
        val url = season.imagePath.orEmpty().tmdbImage

        Image(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .aspectRatio(ratio)
                .clickable { scope.launch { pagerState.animateScrollToPage(i) } },
            url = url,
            contentDescription = season.title
        )

    }

}