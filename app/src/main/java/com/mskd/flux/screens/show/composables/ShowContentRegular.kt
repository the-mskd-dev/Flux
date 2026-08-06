package com.mskd.flux.screens.show.composables

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.room.util.TableInfo
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.features.catalog.presentation.CatalogIntent
import com.mskd.flux.features.show.presentation.ShowIntent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.screens.artwork.composables.common.ArtworkImageFull
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.component.media.MediaItem
import com.mskd.flux.ui.component.media.OverviewItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.PortraitPreview
import com.mskd.flux.utils.extensions.gridItemPadding
import com.mskd.flux.utils.itemWidthFor
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

    val startPadding = PaddingValues(start = FluxUI.Space.medium)
    val lastPadding = PaddingValues(end = FluxUI.Space.medium)
    val middlePadding = PaddingValues(horizontal = FluxUI.Space.small)

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(columns)
    ) {

        item(span = { GridItemSpan(maxLineSpan) }) {

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
                    modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                    title = stringResource(Res.string.summary),
                    description = fullShow.artwork.description.ifEmpty { stringResource(Res.string.no_summary) },
                )

                Text.Content.Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FluxUI.Space.medium),
                    text = stringResource(Res.string.seasons),
                    color = MaterialTheme.colorScheme.onBackground
                )

            }

        }

        itemsIndexed(items = fullShow.seasons, key = { _,s -> s.season }) { index, season ->

            SeasonItem(
                modifier = Modifier
                    .gridItemPadding(index = index, columns = columns)
                    .fillMaxWidth(),
                season = season,
                episodes = fullShow.episodes.filter { it.season == season.season },
                onTap = { sendIntent(ShowIntent.OnSeasonTap(season = season.season, rgb = it))},
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