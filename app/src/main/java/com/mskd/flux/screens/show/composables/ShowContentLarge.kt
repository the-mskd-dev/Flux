package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.screens.show.ShowIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.OverviewItem
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.LandscapePreview

@Composable
fun ShowContentLarge(
    fullShow: FullArtwork.FullShow,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ShowIntent) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.weight(.5f),) {

            ArtworkImage(
                modifier = Modifier.fillMaxSize(),
                fullArtwork = fullShow,
                orientation = Orientation.Horizontal,
            )

        }

        LazyVerticalGrid(
            modifier = Modifier.weight(.5f),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(Ui.Space.small),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.small),
            contentPadding = PaddingValues(horizontal = Ui.Space.medium)
        ) {

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                Text.Display.Small(
                    modifier = Modifier
                        .padding(Ui.Space.medium)
                        .wrapContentWidth(),
                    text = fullShow.artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    emphasized = true
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(Ui.Space.large))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                OverviewItem(
                    modifier = Modifier.padding(horizontal = Ui.Space.medium),
                    title = stringResource(R.string.summary),
                    description = fullShow.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(Ui.Space.large))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                Text.Title.Large(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Ui.Space.medium),
                    text = stringResource(R.string.seasons),
                    emphasized = true,
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(Ui.Space.medium))
            }

            items(
                items = fullShow.seasons,
                key = { it.id }
            ) { season ->

                Box(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    SeasonItem(
                        modifier = Modifier.width(Ui.Dimension.itemWidth),
                        season = season,
                        episodes = fullShow.episodes.filter { it.season == season.season },
                        onTap = { sendIntent(ShowIntent.OnSeasonTap(season = season.season, rgb = it))},
                        onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
                    )

                }


            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateBottomPadding()))
            }

        }

    }

}

@LandscapePreview
@Composable
fun ShowContentLarge_Preview() {
    AppTheme {
        ShowContentLarge(
            fullShow = MediaMockups.fullShow,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}