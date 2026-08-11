package com.mskd.flux.core.network.tmdb.data.dto.show

import com.mskd.flux.core.network.tmdb.data.dto.CrewDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents an episode of a TV show retrieved from TMDB.
 *
 * @property title Title of the episode.
 * @property description Overview or synopsis of the episode.
 * @property id Unique identifier for the episode.
 * @property duration Duration of the episode in minutes.
 * @property number Episode number within the season.
 * @property season Season number containing the episode.
 * @property imagePath Path to the still image of the episode.
 * @property voteAverage Average rating of the episode.
 * @property voteCount Number of votes for the episode.
 * @property releaseDateString Air date of the episode as a string.
 * @property crew List of crew members associated with the episode.
 */
@Serializable
data class EpisodeDto(
    val id: Long,
    @SerialName("show_id")
    val artworkId: Long?,
    @SerialName("name")
    val title: String,
    @SerialName("overview")
    val description: String,
    @SerialName("runtime")
    val duration: Int?,
    @SerialName("episode_number")
    val number: Int,
    @SerialName("season_number")
    val season: Int,
    @SerialName("still_path")
    val imagePath: String?,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("air_date")
    val releaseDateString: String,
    val crew: List<CrewDto>
)
