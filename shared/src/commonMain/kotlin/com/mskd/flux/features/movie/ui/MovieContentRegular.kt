package com.mskd.flux.features.movie.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.ArtworkPageType
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.features.artwork.ui.composables.ArtworkButtons
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkHeader
import com.mskd.flux.features.movie.presentation.MovieContent
import com.mskd.flux.features.movie.presentation.MovieIntent
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.PortraitPreview
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MovieContentRegular(
    content: MovieContent,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (MovieIntent) -> Unit,
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
                fullArtwork = content.fullMovie,
                currentMedia = content.fullMovie.movie,
                showTitle = true,
                pageType = ArtworkPageType.MOVIE,
                isImageBlurred = false,
                // The content is already inset by the page padding
                titleHorizontalMargin = FluxUI.Space.medium * 2,
                onImageClick = { sendIntent(MovieIntent.OnArtworkImageClick) },
                buttons = { modifier ->
                    ArtworkButtons(
                        modifier = modifier,
                        media = content.fullMovie.movie,
                        onPlay = { sendIntent(MovieIntent.PlayMedia(media = it)) },
                        onChangeWatchStatus = { sendIntent(MovieIntent.ChangeWatchStatus(media = it)) },
                    )
                }
            )

        }

        item {
            Spacer(modifier = Modifier.height(FluxUI.Space.large))
        }

        item {
            // A movie description is never a spoiler
            ArtworkOverview(
                overview = content.overview,
                isSpoiler = false,
                isBlurred = false,
                onToggleSpoiler = {},
            )
        }

        item {
            Spacer(modifier = Modifier.height(FluxUI.Space.large))
        }

        if (content.people.isNotEmpty()) {
            item { PeopleList(people = content.people, onClick = { sendIntent(MovieIntent.OnPersonClick(person = it)) }) }
        }

        item {
            Spacer(modifier = Modifier
                .padding(bottom = FluxUI.Space.bottomScreen)
                .height(scaffoldInnerPadding.calculateBottomPadding())
            )
        }

    }

}

internal val previewMovieContent = MovieContent(
    fullMovie = MediaMockups.fullMovie,
    overview = ArtworkOverviewContent.MovieOverview(MediaMockups.fullMovie.movie, MediaMockups.fullMovie.genres),
)

@PortraitPreview
@Composable
fun MovieContentRegular_Preview() {
    FluxTheme {
        MovieContentRegular(
            content = previewMovieContent.copy(people = PeopleMockup.all.toImmutableList()),
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}
