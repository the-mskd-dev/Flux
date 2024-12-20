package com.kaem.flux.model.artwork

import com.kaem.flux.model.UserFile
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

/**
 * Represents detailed information about an artwork, such as a movie or an episode.
 *
 * @property releaseDateString The release date of the artwork as a string.
 * @property description Description or synopsis of the artwork.
 * @property voteAverage Average rating of the artwork.
 * @property voteCount Number of votes received for the artwork.
 * @property duration Duration of the artwork in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the artwork.
 * @property releaseDate Parsed release date as a [Date], derived from [releaseDateString].
 */
abstract class Artwork(
    open val artworkId: Long,
    open val title: String,
    open val releaseDateString: String,
    open val description: String,
    open val voteAverage: Float,
    open val voteCount: Int,
    open val duration: Int,
    open var currentTime: Long = 0L,
    open var status: Status = Status.TO_WATCH,
    open val file: UserFile,
) {

    val releaseDate: Date? get() = releaseDateString.parseTMDBDate()

}