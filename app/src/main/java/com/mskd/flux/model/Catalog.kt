package com.mskd.flux.model

import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie

data class Catalog(
    val artworks: List<Artwork>,
    val movies: List<Movie>,
    val episodes: List<Episode>
)
