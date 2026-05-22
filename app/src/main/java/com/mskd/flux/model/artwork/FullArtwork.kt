package com.mskd.flux.model.artwork

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

}