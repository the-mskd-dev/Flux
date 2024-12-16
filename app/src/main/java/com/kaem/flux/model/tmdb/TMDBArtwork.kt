package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

/**
 * Represents an artwork retrieved from TMDB (The Movie Database).
 *
 * @property id Unique identifier for the artwork.
 * @property description Overview or synopsis of the artwork.
 * @property imagePath Path to the artwork's poster image.
 * @property bannerPath Path to the artwork's backdrop image.
 * @property type Type of media (e.g., movie, TV show, or person).
 * @property genres List of genre IDs associated with the artwork.
 * @property popularity Popularity score of the artwork.
 * @property releaseDateString Release date of the artwork as a string.
 * @property voteAverage Average rating of the artwork.
 * @property voteCount Number of votes for the artwork.
 * @property title Title of the artwork.
 * @property originalTitle Original title of the artwork.
 * @property relatedContent List of related artworks (used for persons).
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
 * Represents a paginated list of TMDB artworks.
 *
 * @property page Current page of the results.
 * @property results List of artworks retrieved for the current page.
 * @property pageCount Total number of pages available.
 * @property resultCount Total number of artworks in the result set.
 */
data class TMDBArtworksResult(
    val page: Int,
    val results: List<TMDBArtwork>,
    @SerializedName("total_pages")
    val pageCount: Int,
    @SerializedName("total_results")
    val resultCount: Int
)