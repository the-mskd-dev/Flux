package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName

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
 * @property releaseDateString Release date of the media as a string.
 * @property voteAverage Average rating of the media.
 * @property voteCount Number of votes for the media.
 * @property title Title of the media.
 * @property originalTitle Original title of the media.
 * @property relatedContent List of related medias (used for persons).
 */
data class TMDBArtwork(
    val id: Long,
    @SerializedName("overview")
    val description: String,
    @SerializedName("poster_path")
    val imagePath: String,
    @SerializedName("backdrop_path")
    val bannerPath: String,
    @SerializedName("media_type")
    var type: TMDBMediaType,
    @SerializedName("genre_ids")
    val genres: List<Int>,
    val popularity: Float,
    @SerializedName("first_air_date")
    val releaseDateString: String,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName(value = "title", alternate = ["name"])
    val title: String,
    @SerializedName(value = "original_title", alternate = ["original_name"])
    val originalTitle: String,

    // For Persons
    @SerializedName("known_for")
    val relatedContent: List<TMDBArtwork>
)

/**
 * Represents a paginated list of TMDB medias.
 *
 * @property page Current page of the results.
 * @property results List of medias retrieved for the current page.
 * @property pageCount Total number of pages available.
 * @property resultCount Total number of medias in the result set.
 */
data class TMDBArtworksResult(
    val page: Int,
    val results: List<TMDBArtwork>,
    @SerializedName("total_pages")
    val pageCount: Int,
    @SerializedName("total_results")
    val resultCount: Int
)