package com.mskd.flux.model.artwork

sealed class FullArtwork {

    data class MOVIE(
        val artwork: Artwork,
        val movie: Movie
    ) : FullArtwork()

    data class SHOW(
        val artwork: Artwork,
        val seasons: List<Season>,
        val episodes: List<Episode>
    ) : FullArtwork()

}