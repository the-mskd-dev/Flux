package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TMDBSeason(
    val id: Long,
    @SerialName("name")
    val title: String,
    @SerialName("overview")
    val description: String,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("season_number")
    val season: Int,
    val episodes: List<TMDBEpisode>
)
