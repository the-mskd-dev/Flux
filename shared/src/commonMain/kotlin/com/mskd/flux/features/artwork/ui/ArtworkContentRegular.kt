package com.mskd.flux.features.artwork.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkHeader
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.media.episode.EpisodeDropDownMenu
import com.mskd.flux.ui.media.episode.EpisodeItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episodes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtworkContentRegular(
    fullArtwork: FullArtwork,
    people: List<Person>,
    selectedMedia: Media,
    selectedSeason: Int?,
    overview: ArtworkOverviewContent,
    isOverviewSpoiler: Boolean,
    isOverviewBlurred: Boolean,
    expandedEpisodeId: Long?,
    spoilerEpisodeIds: Set<Long>,
    blurredEpisodeIds: Set<Long>,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {

            ArtworkHeader(
                modifier = Modifier.fillMaxWidth(),
                fullArtwork = fullArtwork,
                currentMedia = selectedMedia,
                selectedSeason = selectedSeason,
                title = fullArtwork.artwork.title,
                sendIntent = sendIntent
            )

        }

        item {
            Spacer(modifier = Modifier.height(FluxUI.Space.large))
        }

        item {

            ArtworkOverview(
                overview = overview,
                isSpoiler = isOverviewSpoiler,
                isBlurred = isOverviewBlurred,
                onToggleSpoiler = { sendIntent(ArtworkIntent.ToggleOverviewSpoiler) },
            )

        }

        item {
            Spacer(modifier = Modifier.height(FluxUI.Space.large))
        }

        (fullArtwork as? FullArtwork.FullShow)?.let { show ->

            val episodes = show.episodes.filter { it.season == selectedSeason }

            if (episodes.isNotEmpty()) {

                item {

                    Column(verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)) {

                        Text.Content.Title(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.episodes),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                    }

                }

                item {
                    Spacer(modifier = Modifier.height(FluxUI.Space.medium))
                }

                items(
                    items = episodes.sortedBy { it.number },
                    key = { e -> e.id to e.currentTime }
                ) { episode ->


                    EpisodeItem(
                        modifier = Modifier.animateItem(),
                        episode = episode,
                        isSelected = episode.id == selectedMedia.mediaId,
                        onClick = { sendIntent(ArtworkIntent.PlayMedia(media = episode)) },
                        isExpanded = episode.id == expandedEpisodeId,
                        isBlurred = episode.id in blurredEpisodeIds,
                        isSpoiler = episode.id in spoilerEpisodeIds,
                        onToggleSpoiler = { sendIntent(ArtworkIntent.ToggleEpisodeSpoiler(episode = episode)) },
                        onReadMoreTap = {
                            if (it) {
                                sendIntent(ArtworkIntent.ExpandEpisodeDescription(episode = episode))
                            } else {
                                sendIntent(ArtworkIntent.CollapseEpisodeDescription)
                            }
                        },
                        dropDownMenu = { isVisible, onDismissRequest ->
                            EpisodeDropDownMenu(
                                isVisible = isVisible,
                                episode = episode,
                                onDismissRequest = onDismissRequest,
                                sendIntent = sendIntent
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(FluxUI.Space.small))

                }

            }

        }

        if (people.isNotEmpty()) {
            item { PeopleList(people = people, onClick = { sendIntent(ArtworkIntent.OnPersonClick(person = it)) }) }
        }

        item {
            Spacer(modifier = Modifier
                .padding(bottom = FluxUI.Space.bottomScreen)
                .height(scaffoldInnerPadding.calculateBottomPadding())
            )
        }

    }

}

@PortraitPreview
@Composable
fun ArtworkContentMovie_Preview() {
    FluxTheme {
        ArtworkContentRegular(
            fullArtwork = MediaMockups.fullMovie,
            people = PeopleMockup.all,
            selectedMedia = MediaMockups.movie,
            selectedSeason = null,
            overview = ArtworkOverviewContent.MovieOverview(MediaMockups.fullMovie.movie, MediaMockups.fullMovie.genres),
            isOverviewSpoiler = false,
            isOverviewBlurred = false,
            expandedEpisodeId = null,
            spoilerEpisodeIds = emptySet(),
            blurredEpisodeIds = emptySet(),
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}

@PortraitPreview
@Composable
fun ArtworkContentShow_Preview() {
    FluxTheme {
        ArtworkContentRegular(
            fullArtwork = MediaMockups.fullShow,
            people = PeopleMockup.all,
            selectedMedia = MediaMockups.episode1,
            selectedSeason = 1,
            overview = ArtworkOverviewContent.Summary(MediaMockups.season1.description),
            isOverviewSpoiler = false,
            isOverviewBlurred = false,
            expandedEpisodeId = null,
            spoilerEpisodeIds = emptySet(),
            blurredEpisodeIds = emptySet(),
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}