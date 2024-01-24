package com.kaem.flux.model.flux

data class FluxMovie(
    override val id: Int,
    override val name: String,
    override val imagePath: String,
    override val bannerPath: String,
    override val releaseDateString: String,
    val description: String,
    val voteAverage: Float,
    val voteCount: Int,
    val genres: List<String> = listOf(),
    val duration: Int,
    var isWatched: Boolean = false,
    val file: FluxFile
) : FluxArtwork
