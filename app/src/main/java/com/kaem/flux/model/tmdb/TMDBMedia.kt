package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBMedia(
    val id: Int,
    val name: String,
    val overview: String,
    val backdrop_path: String,
    val poster_path: String,
    @SerializedName("media_type")
    val type: TMDBMediaType,
    @SerializedName("genre_ids")
    val genres: List<Int>,
    val popularity: Float,
    @SerializedName("first_air_date")
    val releaseDateString: String,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int
)

data class TMDBMediasResult(
    val page: Int,
    val results: List<TMDBMedia>,
    val total_pages: Int,
    val total_results: Int
)