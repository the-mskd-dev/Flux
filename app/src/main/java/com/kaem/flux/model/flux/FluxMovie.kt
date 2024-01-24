package com.kaem.flux.model.flux

data class FluxMovie(
    override val id: Int,
    override val name: String,
    override val imagePath: String,
    override val bannerPath: String,
    override val releaseDateString: String,
    override val description: String,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val duration: Int,
    override var isWatched: Boolean = false,
    override val file: FluxFile,
    val genres: List<String> = listOf(),
) : FluxArtwork, FluxArtworkDetails
