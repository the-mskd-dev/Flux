package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.mskd.flux.R
import com.mskd.flux.data.repository.customization.LocalCustomization
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.model.artwork.FullArtwork
import com.mskd.flux.screens.artwork.composables.common.ArtworkImage
import com.mskd.flux.screens.artwork.composables.common.ArtworkImageFull
import com.mskd.flux.screens.show.ShowIntent
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.OverviewItem
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.PortraitPreview
import com.mskd.flux.utils.itemWidthFor
import com.mskd.flux.utils.rememberScreenDimensions

@Composable
fun ShowContentRegular(
    fullShow: FullArtwork.FullShow,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ShowIntent) -> Unit
) {

    val screenDimensions = rememberScreenDimensions()
    val columns = LocalCustomization.current.itemsPerRow
    val itemWidth = itemWidthFor(screenWidthDp = screenDimensions.widthDp, columns = columns)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {

            ConstraintLayout(modifier = Modifier.fillMaxWidth()) {

                val (image, title) = createRefs()

                ArtworkImageFull(
                    modifier = Modifier
                        .constrainAs(image) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                        .fillMaxWidth()
                        .aspectRatio(Ui.Images.ratio_6_5),
                    fullArtwork = fullShow,
                )

                Text.Adaptive(
                    modifier = Modifier
                        .constrainAs(title) {
                            start.linkTo(parent.start,Ui.Space.medium)
                            end.linkTo(parent.end, Ui.Space.medium)
                            top.linkTo(image.bottom)
                            bottom.linkTo(image.bottom)
                            width = Dimension.preferredWrapContent
                        },
                    text = fullShow.artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displayMediumEmphasized,
                    maxLines = 3,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = MaterialTheme.typography.titleSmallEmphasized.fontSize,
                        maxFontSize = MaterialTheme.typography.displayMediumEmphasized.fontSize,
                    )
                )

            }


        }

        item { Spacer(modifier = Modifier.height(Ui.Space.large)) }

        item {

            OverviewItem(
                modifier = Modifier.padding(horizontal = Ui.Space.medium),
                title = stringResource(R.string.summary),
                description = fullShow.artwork.description.ifEmpty { stringResource(R.string.no_summary) },
            )

        }

        item { Spacer(modifier = Modifier.height(Ui.Space.medium)) }

        item {

            Text.Title.Large(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Ui.Space.medium),
                text = stringResource(R.string.seasons),
                emphasized = true,
                color = MaterialTheme.colorScheme.onBackground
            )

        }

        item { Spacer(modifier = Modifier.height(Ui.Space.medium)) }

        val seasonsChunks = fullShow.seasons.chunked(columns)

        items(
            items = seasonsChunks,
            key = { seasons -> seasons.fold("") { acc, s -> acc + s.id } }
        ) { seasons ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Ui.Space.medium)
                    .padding(bottom = Ui.Space.medium),
                horizontalArrangement = Arrangement.spacedBy(Ui.Space.small)
            ) {

                seasons.forEach { season ->

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {

                        SeasonItem(
                            modifier = Modifier.width(itemWidth),
                            season = season,
                            episodes = fullShow.episodes.filter { it.season == season.season },
                            onTap = { sendIntent(ShowIntent.OnSeasonTap(season = season.season, rgb = it))},
                            onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
                        )

                    }


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

@PortraitPreview
@Composable
fun ShowContentRegular_Preview() {
    AppTheme {
        ShowContentRegular(
            fullShow = MediaMockups.fullShow,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}