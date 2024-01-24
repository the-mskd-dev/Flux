package com.kaem.flux.model.flux

data class FluxArtwork(
    val id: Int,
    val name: String,
    val description: String,
    val duration: Int,
    val imagePath: String,
    val bannerPath: String,
    val voteAverage: Float,
    val voteCount: Int,
    val releaseDateString: String,
    val isWatched: Boolean = false,
    val file: FluxFile
)
