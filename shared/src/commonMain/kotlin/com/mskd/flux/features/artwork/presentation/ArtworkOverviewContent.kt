package com.mskd.flux.features.artwork.presentation

import androidx.compose.runtime.Immutable
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Genre
import com.mskd.flux.core.model.artwork.Movie
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface ArtworkOverviewContent {
    data class MovieOverview(val movie: Movie, val genres: ImmutableList<Genre>) : ArtworkOverviewContent
    data class EpisodeOverview(val episode: Episode) : ArtworkOverviewContent
    /** [description] is null when there is no summary to show. */
    data class Summary(val description: String?) : ArtworkOverviewContent
}
