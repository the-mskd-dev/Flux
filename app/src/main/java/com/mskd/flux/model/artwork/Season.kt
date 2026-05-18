package com.mskd.flux.model.artwork

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an episode of a TV show.
 *
 * @property title Title of the episode.
 * @property artworkId Identifier of the parent show.
 * @property season Season number.
 * @property imagePath Path to the episode's image.
 * @property description Description or synopsis of the media.
 */
@Entity(
    tableName = "seasons",
    indices = [
        Index(value = ["artworkId"])
    ]
)
data class Season(
    @PrimaryKey
    val id: Long,
    val artworkId: Long,
    val title: String,
    val description: String,
    val imagePath: String?,
    val season: Int
)
