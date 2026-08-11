package com.mskd.flux.core.network.tmdb.data.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Represents an media retrieved from TMDB (The Movie Database).
 *
 * @property id Unique identifier for the media.
 * @property description Overview or synopsis of the media.
 * @property imagePath Path to the media's poster image.
 * @property bannerPath Path to the media's backdrop image.
 * @property type Type of media (e.g., movie, TV show, or person).
 * @property genres List of genre IDs associated with the media.
 * @property popularity Popularity score of the media.
 * @property releaseDate Release date of the media as a string.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes for the media.
 * @property title Title of the media.
 * @property originalTitle Original title of the media.
 */
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class ArtworkDto(
    val id: Long,
    @SerialName("overview")
    val description: String,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("backdrop_path")
    val bannerPath: String?,
    @SerialName("genre_ids")
    val genreIds: List<Int>,
    val popularity: Float,
    @JsonNames("release_date", "first_air_date")
    val releaseDate: String?,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("vote_count")
    val voteCount: Int,
    @JsonNames("title", "name")
    val title: String,
    @JsonNames("original_title", "original_name")
    val originalTitle: String,

    @SerialName("media_type")
    var type: MediaTypeDto?,
) {


}