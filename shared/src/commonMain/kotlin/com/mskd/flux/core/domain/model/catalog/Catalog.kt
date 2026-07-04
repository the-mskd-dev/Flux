package com.mskd.flux.core.domain.model.catalog

import com.mskd.flux.core.domain.model.artwork.Artwork
import com.mskd.flux.core.domain.model.artwork.Episode
import com.mskd.flux.core.domain.model.artwork.Movie
import com.mskd.flux.core.domain.model.artwork.Season

data class Catalog(
    val artworks: List<Artwork> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val seasons: List<Season> = emptyList(),
    val episodes: List<Episode> = emptyList()
)