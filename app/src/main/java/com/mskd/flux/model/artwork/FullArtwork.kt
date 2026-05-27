package com.mskd.flux.model.artwork

import com.mskd.flux.utils.extensions.firstEpisodeToWatch

sealed class FullArtwork {

    data class FullMovie(
        val resume: Artwork,
        val movie: Movie
    ) : FullArtwork()

    data class FullShow(
        val resume: Artwork,
        val seasons: List<Season>,
        val episodes: List<Episode>
    ) : FullArtwork()

    val artwork: Artwork get() = when (this) {
        is FullMovie -> this.resume
        is FullShow -> this.resume
    }

    val imagePath: String get() = when (this) {
        is FullMovie -> this.artwork.imagePath
        is FullShow -> {

            when {
                episodes.all { it.status == Status.TO_WATCH } || episodes.all { it.status == Status.WATCHED  } -> this.artwork.imagePath
                else -> episodes.firstEpisodeToWatch?.let { episode ->
                    seasons.find { it.season == episode.season }?.imagePath
                } ?: this.artwork.imagePath
            }

        }
    }

    val isWatching: Boolean get() = when (this) {
        is FullMovie -> this.movie.status == Status.IS_WATCHING
        is FullShow -> !(this.episodes.all { it.status == Status.TO_WATCH } || this.episodes.all { it.status == Status.WATCHED })
    }

    val contentType: ContentType get() = when (this) {
        is FullMovie -> ContentType.MOVIE
        is FullShow -> ContentType.SHOW
    }

}