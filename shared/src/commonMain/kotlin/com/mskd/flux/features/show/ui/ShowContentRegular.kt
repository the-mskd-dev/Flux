package com.mskd.flux.features.show.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.mskd.flux.core.model.artwork.ArtworkPageType
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkHeader
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.features.show.presentation.ShowIntent
import com.mskd.flux.features.show.ui.composables.SeasonItem
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.seasons
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShowContentRegular(
    fullShow: FullArtwork.FullShow,
    people: List<Person>,
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

            ArtworkHeader(
                fullArtwork = fullShow,
                showTitle = true,
                pageType = ArtworkPageType.SHOW,
                onImageClick = { sendIntent(ShowIntent.OnArtworkImageClick) },
            )

        }

        item(span = { GridItemSpan(maxLineSpan) }) {

            Column(
                modifier = Modifier
                    .padding(top = FluxUI.Space.large)
                    .padding(bottom = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                ArtworkOverview(
                    overview = ArtworkOverviewContent.ShowOverview(
                        description = fullShow.artwork.description.ifEmpty { null },
                        genres = fullShow.genres,
                    )
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
                onClick = {
                    sendIntent(
                        ShowIntent.OnSeasonClick(
                            season = season.season,
                            rgb = it
                        )
                    )
                },
                onLongPress = { sendIntent(ShowIntent.ShowSeasonPreview(season = season)) }
            )

        }

        if (people.isNotEmpty()) {

            item(span = { GridItemSpan(maxLineSpan) }) {

                PeopleList(people = people, onClick = { sendIntent(ShowIntent.OnPersonClick(person = it)) })

            }

        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier
                .padding(bottom = FluxUI.Space.bottomScreen)
                .height(scaffoldInnerPadding.calculateBottomPadding())
            )
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
            people = PeopleMockup.all,
            sendIntent = {}
        )
    }
}