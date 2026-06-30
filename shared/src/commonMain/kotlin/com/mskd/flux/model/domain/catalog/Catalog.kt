package com.mskd.flux.model.domain.catalog

import com.mskd.flux.model.domain.artwork.Artwork
import com.mskd.flux.model.domain.artwork.Episode
import com.mskd.flux.model.domain.artwork.Movie
import com.mskd.flux.model.domain.artwork.Season

data class Catalog(
    val artworks: List<Artwork> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val episodes: List<Episode> = emptyList()
)