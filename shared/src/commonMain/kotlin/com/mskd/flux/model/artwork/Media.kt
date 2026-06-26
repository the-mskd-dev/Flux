package com.mskd.flux.model.artwork

import com.mskd.flux.model.Status
import com.mskd.flux.model.UserFile
import com.mskd.flux.utils.extensions.minToMs

/**
 * Represents detailed information about a media, such as a movie or an episode.
 *
 * @property artworkId ID of the artwork
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
 */
sealed class Media {
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

    open val mediaId: Long get() = artworkId

    val progressPercent: Float get() = this.currentTime.toFloat() / this.duration.minToMs

}