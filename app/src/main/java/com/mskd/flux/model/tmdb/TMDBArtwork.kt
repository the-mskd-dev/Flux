package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName
import com.mskd.flux.utils.Levenshtein
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 */
@Serializable
data class TMDBArtwork(
    val id: Long,
    @SerialName("overview")
    val description: String,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("backdrop_path")
    val bannerPath: String?,
    @SerialName("media_type")
    var type: TMDBMediaType,
    @SerialName("genre_ids")
    val genres: List<Int>,
    val popularity: Float,
    @SerialName("release_date")
    val releaseDateString: String?,
    @SerialName("first_air_date")
    val firstAirDateString: String?,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("vote_count")
    val voteCount: Int,

    @SerialName("title")
    val title: String?,
    @SerialName("name")
    val name: String?,

    @SerialName(value = "original_title")
    val originalTitle: String?,
    @SerialName("original_name")
    val originalName: String?,
)

/**
 * Represents a paginated list of TMDB medias.
 *
 * @property page Current page of the results.
 * @property results List of medias retrieved for the current page.
 * @property pageCount Total number of pages available.
 * @property resultCount Total number of medias in the result set.
 */
@Serializable
data class TMDBArtworksResult(
    val page: Int,
    val results: List<TMDBArtwork>,
    @SerialName("total_pages")
    val pageCount: Int,
    @SerialName("total_results")
    val resultCount: Int
) {

    fun artworkFor(fileName: String) : TMDBArtwork? {
        return results.minByOrNull {
            Levenshtein.minDistance(
                query = fileName,
                title = it.title ?: it.name ?: return null,
                originalTitle = it.originalTitle ?: it.originalName ?: return null
            )
        }
    }

}