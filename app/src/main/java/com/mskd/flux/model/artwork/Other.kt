package com.mskd.flux.model.artwork

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mskd.flux.model.UserFile

/**
 * Represents an unknown media, such as a movie or a TV show.
 *
 * @property id Unique identifier for the episode.
 * @property artworkId Constant identifier.
 * @property releaseDateString The release date of the media as a string.
 * @property description Description or synopsis of the media.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes received for the media.
 * @property duration Duration of the media in minutes.
 * @property currentTime Current playback position in milliseconds.
 * @property file The associated local file.
 * @property status Viewing status of the media.
 */
@Entity(
    tableName = "other",
    foreignKeys = [
        ForeignKey(
            entity = Artwork::class,
            parentColumns = ["id"],
            childColumns = ["artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["artworkId"])
    ]
)
data class Other(
    @PrimaryKey
    val id: Long,
    override val artworkId: Long = ARTWORK_ID,
    override val title: String,
    override val releaseDateString: String = "",
    override val description: String = "",
    override val voteAverage: Float = 0f,
    override val voteCount: Int = 0,
    override val duration: Int,
    override val currentTime: Long = 0L,
    override val status: Status = Status.TO_WATCH,
    @Embedded override val file: UserFile,
) : Media() {


    companion object {

        const val ARTWORK_ID : Long = -616

        val Artwork = Artwork(
            id = ARTWORK_ID,
            title = "Other",
            type = ContentType.OTHER
        )

    }

}