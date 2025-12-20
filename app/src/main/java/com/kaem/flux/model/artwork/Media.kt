package com.kaem.flux.model.artwork

import com.kaem.flux.model.UserFile
import com.kaem.flux.utils.extensions.parseTMDBDate
import java.util.Date

/**
 * Represents detailed information about a media, such as a movie or an episode.
 *
 * @property artworkId Id of the artwork
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
 * @property releaseDate Parsed release date as a [Date], derived from [releaseDateString].
 */
abstract class Media {
    abstract val artworkId: Long
    abstract val title: String
    abstract  val releaseDateString: String
    abstract  val description: String
    abstract  val voteAverage: Float
    abstract  val voteCount: Int
    abstract  val duration: Int
    abstract  val currentTime: Long
    abstract  val status: Status

    abstract  val file: UserFile

    val releaseDate: Date? get() = releaseDateString.parseTMDBDate()

}