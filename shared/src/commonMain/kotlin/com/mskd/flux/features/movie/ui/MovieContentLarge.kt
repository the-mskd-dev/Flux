package com.mskd.flux.features.movie.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mskd.flux.core.model.artwork.ArtworkPageType
import com.mskd.flux.features.artwork.ui.composables.ArtworkButtons
import com.mskd.flux.features.artwork.ui.composables.ArtworkImage
import com.mskd.flux.features.artwork.ui.composables.ArtworkOverview
import com.mskd.flux.features.artwork.ui.composables.ArtworkTitle
import com.mskd.flux.features.movie.presentation.MovieContent
import com.mskd.flux.features.movie.presentation.MovieIntent
import com.mskd.flux.features.people.ui.components.people.PeopleList
import com.mskd.flux.mockups.PeopleMockup
import com.mskd.flux.ui.LandscapePreview
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import kotlinx.collections.immutable.toImmutableList

@Composable
fun MovieContentLarge(
    content: MovieContent,
    scaffoldInnerPadding: PaddingValues,
    sendIntent: (MovieIntent) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.weight(.5f)) {

            ArtworkImage(
                modifier = Modifier.fillMaxSize(),
                fullArtwork = content.fullMovie,
                currentMedia = content.fullMovie.movie,
                orientation = Orientation.Horizontal,
                isBlurred = false,
                pageType = ArtworkPageType.MOVIE,
                onClick = { sendIntent(MovieIntent.OnArtworkImageClick) }
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
                ArtworkTitle(
                    modifier = Modifier.wrapContentWidth(),
                    artwork = content.fullMovie.artwork,
                )
            }

            item {
                Spacer(modifier = Modifier.height(FluxUI.Space.large))
            }

            item {
                ArtworkButtons(
                    media = content.fullMovie.movie,
                    onPlay = { sendIntent(MovieIntent.PlayMedia(media = it)) },
                    onChangeWatchStatus = { sendIntent(MovieIntent.ChangeWatchStatus(media = it)) },
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

}

@LandscapePreview
@Composable
fun MovieContentLarge_Preview() {
    FluxTheme {
        MovieContentLarge(
            content = previewMovieContent.copy(people = PeopleMockup.all.toImmutableList()),
            scaffoldInnerPadding = PaddingValues.Zero,
            sendIntent = {}
        )
    }
}
