package com.mskd.flux.core.model.catalog

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Season

data class Catalog(
    val artworks: List<Artwork> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val episodes: List<Episode> = emptyList()
)