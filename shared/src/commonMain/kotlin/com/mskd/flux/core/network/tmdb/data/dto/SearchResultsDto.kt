package com.mskd.flux.core.network.tmdb.data.dto

import com.mskd.flux.utils.Levenshtein
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a paginated list of TMDB medias.
 *
 * @property page Current page of the results.
 * @property results List of medias retrieved for the current page.
 * @property pageCount Total number of pages available.
 * @property resultCount Total number of medias in the result set.
 */
@Serializable
data class SearchResultsDto(
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