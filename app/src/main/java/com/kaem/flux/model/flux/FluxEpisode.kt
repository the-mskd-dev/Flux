package com.kaem.flux.model.flux

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.tmdb.TMDBCrew

data class FluxEpisode(
    val id: Int,
    val name: String,
    val number: Int,
    val season: Int,
    val imagePath: String,
    val releaseDateString: String,
    val crew: List<TMDBCrew>,
    override val description: String,
    override val duration: Int,
    override val voteAverage: Float,
    override val voteCount: Int,
    override var isWatched: Boolean = false,
    override val file: FileSource
) : FluxArtwork
