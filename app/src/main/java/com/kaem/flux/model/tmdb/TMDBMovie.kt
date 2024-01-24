package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBMovie(
    val title: String,
    @SerializedName("overview")
    val description: String,
    val id: Int,
    @SerializedName("runtime")
    val duration: Int,
    @SerializedName("poster_path")
    val imagePath: String,
    @SerializedName("backdrop_path")
    val bannerPath: String,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("release_date")
    val releaseDateString: String,
    val crew: List<TMDBCrew>,
)
