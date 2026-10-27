package com.mskd.flux.features.artwork.ui.composables

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
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

@Composable
fun ArtworkOverview(
    overview: ArtworkOverviewContent,
    isSpoiler: Boolean = false,
    isBlurred: Boolean = false,
    onToggleSpoiler: () -> Unit = {},
) {

    Card(shape = FluxUI.shapes.corners) {

        when (overview) {

            is ArtworkOverviewContent.MovieOverview -> {
                OverviewItem(
                    title = stringResource(Res.string.summary),
                    description = overview.movie.description,
                    subtitle = { MediaDetailsHorizontal(overview.movie) },
                    topDetails = { GenresTags(genres = overview.genres) }
                )
            }

            is ArtworkOverviewContent.EpisodeOverview -> {
                OverviewItem(
                    title = overview.episode.title,
                    description = overview.episode.description,
                    topDetails = { EpisodesDetails(episode = overview.episode) },
                    subtitle = { MediaDetailsHorizontal(media = overview.episode) }
                )
            }

            is ArtworkOverviewContent.Summary -> {
                OverviewItem(
                    title = stringResource(Res.string.summary),
                    description = overview.description ?: stringResource(Res.string.no_summary),
                    isSpoiler = isSpoiler,
                    isBlurred = isBlurred,
                    onToggleSpoiler = onToggleSpoiler,
                )
            }

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
