package com.kaem.flux.model.flux

data class FluxMovie(
    val id: Int,
    val title: String,
    val posterPath: String,
    val bannerPath: String,
    val description: String,
    val releaseDateString: String,
    val voteAverage: Float,
    val voteCount: Int,
    val genres: List<String> = listOf(),
    val duration: Int,
    val isWatched: Boolean = false,
    val file: FluxFile
)
