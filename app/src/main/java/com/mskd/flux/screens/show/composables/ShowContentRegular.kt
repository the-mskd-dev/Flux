package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mskd.flux.R
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.screens.show.ShowIntent
import com.mskd.flux.ui.component.OverviewItem
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.Ui
import kotlin.collections.chunked
import kotlin.text.ifEmpty

@Composable
fun ShowContentRegular(
    fullShow: FullArtwork.FullShow,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ShowIntent) -> Unit
) {

    val columns = 3

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {

            Column(
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                ArtworkImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(Ui.Images.RATIO_6_5),
                    fullArtwork = fullShow,
                )

                OverviewItem(
                    modifier = Modifier.padding(horizontal = Ui.Space.MEDIUM),
                    title = stringResource(R.string.summary),
                    description = fullShow.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
                )

                Text.Title.Large(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Ui.Space.MEDIUM),
                    text = stringResource(R.string.seasons),
                    emphasized = true,
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

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