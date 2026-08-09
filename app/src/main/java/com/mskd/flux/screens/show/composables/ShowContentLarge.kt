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
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.features.show.presentation.ShowIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.artwork.composables.common.ArtworkImageFull
import com.mskd.flux.screens.artwork.composables.common.GenresTags
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.OverviewItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.LandscapePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.seasons
import flux.shared.generated.resources.summary
import org.jetbrains.compose.resources.stringResource

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

            ArtworkImageFull(
                modifier = Modifier.fillMaxSize(),
                fullArtwork = fullShow,
                orientation = Orientation.Horizontal,
            )

        }

        LazyVerticalGrid(
            modifier = Modifier.weight(.5f),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                Text.MainTitle(
                    modifier = Modifier
                        .padding(FluxUI.Space.medium)
                        .wrapContentWidth(),
                    text = fullShow.artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                OverviewItem(
                    modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                    title = stringResource(Res.string.summary),
                    description = fullShow.artwork.description.ifEmpty { stringResource(Res.string.no_summary) },
                    topDetails = { GenresTags(genres = fullShow.genres) }
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item(span = { GridItemSpan(maxLineSpan) }) {

                Text.Content.Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FluxUI.Space.medium),
                    text = stringResource(Res.string.seasons),
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(FluxUI.Space.medium))
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
                        modifier = Modifier.width(FluxUI.Dimension.itemWidth),
                        season = season,
                        episodes = fullShow.episodes.filter { it.season == season.season },
                        onClick = { sendIntent(ShowIntent.OnSeasonClick(season = season.season, rgb = it))},
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
    FluxTheme {
        ShowContentLarge(
            fullShow = MediaMockups.fullShow,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}