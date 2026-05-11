package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBSeason(
    val id: Long,
    val name: String,
    val overview: String,
    @SerializedName("poster_path")
    val imagePath: String,
    @SerializedName("season_number")
    val season: Int
)
