package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mskd.flux.features.artwork.presentation.ArtworkOverviewContent
import com.mskd.flux.mockups.MediaMockups
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.media.EpisodesDetails
import com.mskd.flux.ui.media.MediaDetailsHorizontal
import com.mskd.flux.ui.media.OverviewItem
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.no_summary
import flux.shared.generated.resources.summary
import org.jetbrains.compose.resources.stringResource

/**
 * Overview of an Artwork page: the summary of the Movie, Season or Show, or the selected Episode, masked when it is a spoiler.
 */
@Composable
fun ArtworkOverview(
    overview: ArtworkOverviewContent,
    modifier: Modifier = Modifier,
    isSpoiler: Boolean = false,
    isBlurred: Boolean = false,
    onToggleSpoiler: () -> Unit = {},
) {

    // The Show page draws its overview without the card, which keeps the overview shadow.
    if (overview is ArtworkOverviewContent.ShowOverview) {
        ArtworkOverviewItem(overview, modifier, isSpoiler, isBlurred, onToggleSpoiler)
    } else {
        Card(modifier = modifier, shape = FluxUI.shapes.corners) {
            ArtworkOverviewItem(overview, Modifier, isSpoiler, isBlurred, onToggleSpoiler)
        }
    }

}

@Composable
private fun ArtworkOverviewItem(
    overview: ArtworkOverviewContent,
    modifier: Modifier,
    isSpoiler: Boolean,
    isBlurred: Boolean,
    onToggleSpoiler: () -> Unit,
) {

    when (overview) {

        is ArtworkOverviewContent.MovieOverview -> {
            OverviewItem(
                modifier = modifier,
                title = stringResource(Res.string.summary),
                description = overview.movie.description,
                subtitle = { MediaDetailsHorizontal(overview.movie) },
                topDetails = { GenresTags(genres = overview.genres) }
            )
        }

        is ArtworkOverviewContent.EpisodeOverview -> {
            OverviewItem(
                modifier = modifier,
                title = overview.episode.title,
                description = overview.episode.description,
                topDetails = { EpisodesDetails(episode = overview.episode) },
                subtitle = { MediaDetailsHorizontal(media = overview.episode) },
                isSpoiler = isSpoiler,
                isBlurred = isBlurred,
                isTitleSpoiler = true,
                onToggleSpoiler = onToggleSpoiler,
            )
        }

        is ArtworkOverviewContent.Summary -> {
            OverviewItem(
                modifier = modifier,
                title = stringResource(Res.string.summary),
                description = overview.description ?: stringResource(Res.string.no_summary),
                isSpoiler = isSpoiler,
                isBlurred = isBlurred,
                onToggleSpoiler = onToggleSpoiler,
            )
        }

        is ArtworkOverviewContent.ShowOverview -> {
            OverviewItem(
                modifier = modifier,
                title = stringResource(Res.string.summary),
                description = overview.description ?: stringResource(Res.string.no_summary),
                topDetails = { GenresTags(genres = overview.genres) }
            )
        }

    }

}

@FluxPreview
@Composable
fun ArtworkOverview_Movie_Preview() {
    FluxTheme {
        ArtworkOverview(
            overview = ArtworkOverviewContent.MovieOverview(
                movie = MediaMockups.fullMovie.movie,
                genres = MediaMockups.fullMovie.genres,
            )
        )
    }
}

@FluxPreview
@Composable
fun ArtworkOverview_Episode_Preview() {
    FluxTheme {
        ArtworkOverview(overview = ArtworkOverviewContent.EpisodeOverview(episode = MediaMockups.episode1))
    }
}

@FluxPreview
@Composable
fun ArtworkOverview_Summary_Preview() {
    FluxTheme {
        ArtworkOverview(overview = ArtworkOverviewContent.Summary(description = MediaMockups.season1.description))
    }
}

@FluxPreview
@Composable
fun ArtworkOverview_Show_Preview() {
    FluxTheme {
        ArtworkOverview(
            overview = ArtworkOverviewContent.ShowOverview(
                description = MediaMockups.fullShow.artwork.description,
                genres = MediaMockups.fullShow.genres,
            )
        )
    }
}
