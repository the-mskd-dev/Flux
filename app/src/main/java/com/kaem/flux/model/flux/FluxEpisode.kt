package com.kaem.flux.model.flux

import com.kaem.flux.model.tmdb.TMDBCrew

data class FluxEpisode(
    val id: Int,
    val name: String,
    val number: Float,
    val season: Int,
    val imagePath: String,
    val releaseDateString: String,
    val crew: List<TMDBCrew>,
    override val description: String,
    override val duration: Int,
    override val voteAverage: Float,
    override val voteCount: Int,
    override var isWatched: Boolean = false,
    override val file: FluxFile
) : FluxArtwork
