package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

abstract class FluxArtworkSummary(
    val id: Int,
    val title: String,
    val imagePath: String,
    val bannerPath: String,
    val releaseDateString: String,
) {

    val releaseDate: Date? get() = releaseDateString.parseTMDBDate()

}

interface FluxArtwork {
    val description: String
    val voteAverage: Float
    val voteCount: Int
    val duration: Int
    var isWatched: Boolean
    val file: UserFile
}