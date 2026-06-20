package com.mskd.flux.shared.model

import com.mskd.flux.shared.model.artwork.Artwork
import com.mskd.flux.shared.model.artwork.Episode
import com.mskd.flux.shared.model.artwork.Movie
import com.mskd.flux.shared.model.artwork.Season

data class Catalog(
    val artworks: List<Artwork> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val episodes: List<Episode> = emptyList()
)