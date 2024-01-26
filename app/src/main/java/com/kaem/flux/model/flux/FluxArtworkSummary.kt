package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile

interface FluxArtworkSummary {
    val id: Int
    val title: String
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
    val file: UserFile
}