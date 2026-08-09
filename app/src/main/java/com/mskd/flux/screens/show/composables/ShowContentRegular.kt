package com.mskd.flux.screens.show.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.features.show.presentation.ShowIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.artwork.composables.common.ArtworkImageFull
import com.mskd.flux.screens.artwork.composables.common.GenresTags
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.OverviewItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.PortraitPreview
import com.mskd.flux.utils.extensions.bleedHorizontal
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.seasons
import flux.shared.generated.resources.summary
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShowContentRegular(
    fullShow: FullArtwork.FullShow,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ShowIntent) -> Unit
) {

    val columns = FluxUI.itemsPerRow.seasons

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.small),
        contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {

            ConstraintLayout(modifier = Modifier
                .bleedHorizontal()
                .fillMaxWidth()
            ) {

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
                        .aspectRatio(FluxUI.Images.ratio_6_5),
                    fullArtwork = fullShow,
                )

                Text.MainTitle(
                    modifier = Modifier
                        .constrainAs(title) {
                            start.linkTo(parent.start,FluxUI.Space.medium)
                            end.linkTo(parent.end, FluxUI.Space.medium)
                            top.linkTo(image.bottom)
                            bottom.linkTo(image.bottom)
                            width = Dimension.preferredWrapContent
                        },
                    text = fullShow.artwork.title,
                    color = MaterialTheme.colorScheme.onBackground,
                )

            }


        }

        item(span = { GridItemSpan(maxLineSpan) }) {

            Column(
                modifier = Modifier
                    .padding(top = FluxUI.Space.large)
                    .padding(bottom = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OverviewItem(
                    title = stringResource(Res.string.summary),
                    description = fullShow.artwork.description.ifEmpty { stringResource(Res.string.no_summary) },
                    topDetails = { GenresTags(genres = fullShow.genres) }
                )

                Text.Content.Title(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.seasons),
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

        }

        items(items = fullShow.seasons, key = { it.season }) { season ->

            SeasonItem(
                modifier = Modifier.fillMaxWidth(),
                season = season,
                episodes = fullShow.episodes.filter { it.season == season.season },
                onClick = { sendIntent(ShowIntent.OnSeasonClick(season = season.season, rgb = it))},
                onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
            )

        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateBottomPadding()))
        }

    }

}

@PortraitPreview
@Composable
fun ShowContentRegular_Preview() {
    FluxTheme {
        ShowContentRegular(
            fullShow = MediaMockups.fullShow,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}