package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

sealed class ArtworkInfo(
    val releaseDateString: String,
    val description: String,
    val voteAverage: Float,
    val voteCount: Int,
    val duration: Int,
    var currentTime: Long = 0L,
    val file: UserFile,
    var status: FluxStatus = FluxStatus.TO_WATCH,
) {

    val releaseDate: Date? get() = releaseDateString.parseTMDBDate()

}