package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName

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
data class TMDBEpisode(
    @SerializedName("name")
    val title: String,
    @SerializedName("overview")
    val description: String,
    val id: Long,
    @SerializedName("runtime")
    val duration: Int,
    @SerializedName("episode_number")
    val number: Int,
    @SerializedName("season_number")
    val season: Int,
    @SerializedName("still_path")
    val imagePath: String,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("air_date")
    val releaseDateString: String,
    val crew: List<TMDBCrew>,
)
