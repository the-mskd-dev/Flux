package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBSeason(
    val id: Long,
    @SerializedName("name")
    val title: String,
    @SerializedName("overview")
    val description: String,
    @SerializedName("poster_path")
    val imagePath: String?,
    @SerializedName("season_number")
    val season: Int,
    val episodes: List<TMDBEpisode>
)
