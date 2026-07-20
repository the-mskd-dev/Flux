package com.mskd.flux.core.network.tmdb.data.dto

import com.mskd.flux.utils.Levenshtein
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
    val genres: List<Int>,
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

/**
 * Represents a paginated list of TMDB medias.
 *
 * @property page Current page of the results.
 * @property results List of medias retrieved for the current page.
 * @property pageCount Total number of pages available.
 * @property resultCount Total number of medias in the result set.
 */
@Serializable
data class ArtworksResultDto(
    val page: Int,
    val results: List<ArtworkDto>,
    @SerialName("total_pages")
    val pageCount: Int,
    @SerialName("total_results")
    val resultCount: Int
) {

    fun artworkFor(fileName: String) : ArtworkDto? {
        return results.minByOrNull {
            Levenshtein.minDistance(
                query = fileName,
                title = it.title,
                originalTitle = it.originalTitle
            )
        }
    }

}