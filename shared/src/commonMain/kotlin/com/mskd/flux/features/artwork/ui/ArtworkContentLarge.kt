package com.mskd.flux.features.artwork.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.mskd.flux.core.model.artwork.FullArtwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.features.artwork.presentation.ArtworkIntent
import com.mskd.flux.features.artwork.ui.composables.ArtworkButtons
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkImage
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.LandscapePreview
import com.mskd.flux.ui.media.episode.EpisodeDropDownMenu
import com.mskd.flux.ui.media.episode.EpisodeItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.episodes
import org.jetbrains.compose.resources.stringResource

@Composable
fun ArtworkContentLarge(
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

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.weight(.5f),) {

            ArtworkImage(
                modifier = Modifier.fillMaxSize(),
                fullArtwork = fullArtwork,
                currentMedia = selectedMedia,
                selectedSeason = selectedSeason,
                orientation = Orientation.Horizontal,
                onClick = { sendIntent(ArtworkIntent.OnArtworkImageClick) }
            )

        }

        LazyColumn(
            modifier = Modifier.weight(.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = FluxUI.Space.medium)
        ) {

            item {
                Spacer(modifier = Modifier.height(scaffoldInnerPadding.calculateTopPadding()))
            }

            item {

                Text.MainTitle(
                    modifier = Modifier.wrapContentWidth(),
                    text = when (fullArtwork) {
                        is FullArtwork.FullMovie -> fullArtwork.artwork.title
                        is FullArtwork.FullShow -> fullArtwork.seasons.find { it.season == selectedSeason }?.title ?: fullArtwork.artwork.title
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                )

            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item {

                ArtworkButtons(
                    media = selectedMedia,
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                        }

                    }

                    item {
                        Spacer(modifier = Modifier.height(FluxUI.Space.medium))
                    }

                    items(
                        items = episodes.sortedBy { it.number },
                        key = { e -> e.id to e.currentTime}
                    ) { episode ->

                        EpisodeItem(
                            modifier = Modifier.animateItem(),
                            episode = episode,
                            isSelected = episode.id == selectedMedia.mediaId,
                            isExpanded = episode.id == expandedEpisodeId,
                            isBlurred = episode.id in blurredEpisodeIds,
                            isSpoiler = episode.id in spoilerEpisodeIds,
                            onToggleSpoiler = { sendIntent(ArtworkIntent.ToggleEpisodeSpoiler(episode = episode)) },
                            onClick = { sendIntent(ArtworkIntent.PlayMedia(media = episode)) },
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

}


@LandscapePreview
@Composable
fun ArtworkContentLargeMovie_Preview() {
    FluxTheme {
        ArtworkContentLarge(
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

@LandscapePreview
@Composable
fun ArtworkContentLargeShow_Preview() {
    FluxTheme {
        ArtworkContentLarge(
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