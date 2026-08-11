package com.mskd.flux.core.model.artwork

import com.mskd.flux.utils.extensions.firstEpisodeToWatch
import kotlinx.collections.immutable.ImmutableList

sealed interface FullArtwork {

    val artwork: Artwork
    val genres: ImmutableList<Genre>

    data class FullMovie(
        override val artwork: Artwork,
        val movie: Movie,
        override val genres: ImmutableList<Genre>
    ) : FullArtwork

    data class FullShow(
        override val artwork: Artwork,
        val seasons: ImmutableList<Season>,
        val episodes: ImmutableList<Episode>,
        override val genres: ImmutableList<Genre>
    ) : FullArtwork

    val imagePath: String get() = when (this) {
        is FullMovie -> this.artwork.imagePath
        is FullShow -> {

            when {
                episodes.all { it.status == Status.TO_WATCH } || episodes.all { it.status == Status.WATCHED  } -> this.artwork.imagePath
                else -> episodes.firstEpisodeToWatch()?.let { episode ->
                    seasons.find { it.season == episode.season }?.imagePath
                } ?: this.artwork.imagePath
            }

        }
    }

    val contentType: ContentType get() = when (this) {
        is FullMovie -> ContentType.MOVIE
        is FullShow -> ContentType.SHOW
    }

    fun isWatching(forSeason: Int?) : Boolean {
        return when (this) {
            is FullMovie -> this.movie.status == Status.IS_WATCHING
            is FullShow -> {
                val filteredEpisodes = this.episodes.filter { it.season == forSeason || forSeason == null }
                !(filteredEpisodes.all { it.status == Status.TO_WATCH } || filteredEpisodes.all { it.status == Status.WATCHED })
            }
        }
    }

}