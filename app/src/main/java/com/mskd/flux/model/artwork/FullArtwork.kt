package com.mskd.flux.model.artwork

sealed class FullArtwork(val artwork: Artwork) {

    class FullMovie(
        artwork: Artwork,
        val movie: Movie
    ) : FullArtwork(artwork)

    class FullShow(
        artwork: Artwork,
        val seasons: List<Season>,
        val episodes: List<Episode>
    ) : FullArtwork(artwork)

}