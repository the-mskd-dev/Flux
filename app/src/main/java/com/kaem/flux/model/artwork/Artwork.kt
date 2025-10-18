package com.kaem.flux.model.artwork

import androidx.room.Embedded
import com.kaem.flux.model.UserFile
import com.kaem.flux.utils.extensions.parseTMDBDate
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
abstract class Artwork {
    abstract val artworkId: Long
    abstract val title: String
    abstract  val releaseDateString: String
    abstract  val description: String
    abstract  val voteAverage: Float
    abstract  val voteCount: Int
    abstract  val duration: Int
    abstract  var currentTime: Long
    abstract  var status: Status

    abstract  val file: UserFile

    val releaseDate: Date? get() = releaseDateString.parseTMDBDate()

}