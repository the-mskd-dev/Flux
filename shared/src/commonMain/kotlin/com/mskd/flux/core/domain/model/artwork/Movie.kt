package com.mskd.flux.core.domain.model.artwork

import com.mskd.flux.core.domain.model.files.UserFile

/**
 * Represents a movie with specific details.
 *
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
 */
data class Movie(
    override val artworkId: Long,
    override val title: String,
    override val releaseDateString: String,
    override val description: String,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val duration: Int,
    override val currentTime: Long = 0L,
    override val status: Status = Status.TO_WATCH,
    override val file: UserFile,
) : Media()