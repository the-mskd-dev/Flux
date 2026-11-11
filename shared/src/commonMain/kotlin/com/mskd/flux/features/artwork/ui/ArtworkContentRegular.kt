package com.mskd.flux.features.artwork.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.mskd.flux.features.artwork.ui.composables.ArtworkButtons
import com.mskd.flux.features.artwork.ui.composables.ArtworkPageHeader
import com.mskd.flux.features.artwork.ui.composables.episodeList
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI

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
    isArtworkImageBlurred: Boolean,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (ArtworkIntent) -> Unit,
) {

    val episodesTitleColor = MaterialTheme.colorScheme.onBackground

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = FluxUI.Space.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        item {

            ArtworkPageHeader(
                modifier = Modifier.fillMaxWidth(),
                fullArtwork = fullArtwork,
                currentMedia = selectedMedia,
                selectedSeason = selectedSeason,
                showTitle = fullArtwork is FullArtwork.FullMovie,
                allowBlurredHeader = true,
                isImageBlurred = isArtworkImageBlurred,
                // The content is already inset by the page padding
                titleHorizontalMargin = FluxUI.Space.medium * 2,
                onImageClick = { sendIntent(ArtworkIntent.OnArtworkImageClick) },
                buttons = { modifier -> ArtworkButtons(modifier = modifier, media = selectedMedia, sendIntent = sendIntent) }
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
            episodeList(
                episodes = show.episodes.filter { it.season == selectedSeason },
                selectedMediaId = selectedMedia.mediaId,
                expandedEpisodeId = expandedEpisodeId,
                spoilerEpisodeIds = spoilerEpisodeIds,
                blurredEpisodeIds = blurredEpisodeIds,
                titleColor = episodesTitleColor,
                sendIntent = sendIntent,
            )
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
            isArtworkImageBlurred = false,
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
            isArtworkImageBlurred = false,
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}