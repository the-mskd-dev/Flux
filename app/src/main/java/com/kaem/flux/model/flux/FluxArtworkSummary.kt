package com.kaem.flux.model.flux

interface FluxArtworkSummary {
    val id: Int
    val name: String
    val imagePath: String
    val bannerPath: String
    val releaseDateString: String
}

interface FluxArtwork {
    val description: String
    val voteAverage: Float
    val voteCount: Int
    val duration: Int
    var isWatched: Boolean
    val file: FluxFile
}