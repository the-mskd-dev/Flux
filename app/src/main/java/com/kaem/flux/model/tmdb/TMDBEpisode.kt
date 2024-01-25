package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBEpisode(
    @SerializedName("name")
    val title: String,
    @SerializedName("overview")
    val description: String,
    val id: Int,
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
