package com.kaem.flux.model.flux

import com.kaem.flux.model.tmdb.TMDBCrew

data class FluxEpisode(
    val name: String,
    val description: String,
    val id: Int,
    val duration: Int,
    val number: Float,
    val season: Int,
    val imagePath: String,
    val voteAverage: Float,
    val voteCount: Int,
    val releaseDateString: String,
    val crew: List<TMDBCrew>,
    var isWatched: Boolean = false,
    val file: FluxFile
)
