package com.mskd.flux.core.model.artwork

import androidx.compose.runtime.Stable
import com.mskd.flux.core.model.files.UserFile

/**
 * Represents an episode of a TV show.
 *
 * @property id Unique identifier for the episode.
 * @property title Title of the episode.
 * @property artworkId Identifier of the parent show.
 * @property number Episode number.
 * @property season Season number.
 * @property imagePath Path to the episode's image.
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
 * @property releaseDateString Release date of the episode as a string.
 */
@Stable
data class Episode(
    val id: Long,
    val number: Int,
    val season: Int,
    val imagePath: String,
    override val artworkId: Long,
    override val title: String,
    override val releaseDateString: String,
    override val description: String,
    override val duration: Int,
    override val currentTime: Long = 0L,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val file: UserFile,
    override val status: Status = Status.TO_WATCH,
    override val isAvailable: Boolean
) : Media() {

    override val mediaId: Long get() = id

    constructor(file: UserFile, duration: Int = 0) : this (
        id = -file.path.hashCode().toLong(),
        artworkId = Artwork.UNKNOWN_ID,
        title = file.nameProperties.title,
        number = file.nameProperties.episode ?: -1,
        season = file.nameProperties.season ?: -1,
        imagePath = "",
        releaseDateString = "",
        description = "",
        duration = duration,
        currentTime = 0L,
        voteAverage = 0f,
        voteCount = 0,
        status = Status.TO_WATCH,
        file = file,
        isAvailable = true
    )

    val isUnknown: Boolean get() = artworkId == Artwork.UNKNOWN_ID

    val infoUrl: String get() = "https://www.themoviedb.org/tv/$artworkId/season/$season/episode/$number"

}
