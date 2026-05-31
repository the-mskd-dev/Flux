package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mskd.flux.ui.component.OverviewItem
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.LandscapePreview
import com.mskd.flux.utils.PortraitPreview
import kotlin.collections.chunked
import kotlin.text.ifEmpty

@Composable
fun ShowContentLarge(
    fullShow: FullArtwork.FullShow,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ShowIntent) -> Unit
) {

    val columns = 3

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

        LazyColumn(
            modifier = Modifier.weight(.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            item {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item {

                Text.Display.Small(
                    modifier = Modifier
                        .padding(Ui.Space.MEDIUM)
                        .wrapContentWidth(),
                    text = fullShow.artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    emphasized = true
                )

            }

            item {
                Spacer(modifier = Modifier.height(Ui.Space.LARGE))
            }

            item {

                OverviewItem(
                    modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM),
                    title = stringResource(R.string.summary),
                    description = fullShow.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
                )

            }

            item {
                Spacer(modifier = Modifier.height(Ui.Space.LARGE))
            }

            item {

                Text.Title.Large(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Ui.Space.MEDIUM),
                    text = stringResource(R.string.seasons),
                    emphasized = true,
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

            item {
                Spacer(modifier = Modifier.height(Ui.Space.MEDIUM))
            }

            val seasonsChunks = fullShow.seasons.chunked(columns)

            items(
                items = seasonsChunks,
                key = { seasons -> seasons.fold("") { acc, s -> acc + s.id } }
            ) { seasons ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Ui.Space.MEDIUM)
                        .padding(bottom = Ui.Space.MEDIUM),
                    horizontalArrangement = Arrangement.spacedBy(Ui.Space.SMALL)
                ) {

                    seasons.forEach { season ->

                        SeasonItem(
                            modifier = Modifier.weight(1f),
                            season = season,
                            episodes = fullShow.episodes.filter { it.season == season.season },
                            onTap = { sendIntent(ShowIntent.OnSeasonTap(season = season.season, rgb = it))},
                            onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
                        )

                    }

                    val emptySlots = columns - seasons.size
                    if (emptySlots > 0) {
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                }


            }

            item {
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