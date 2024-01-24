package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBMedia(
    val id: Int,
    val name: String,
    @SerializedName("overview")
    val description: String,
    @SerializedName("poster_path")
    val posterPath: String,
    @SerializedName("backdrop_path")
    val backdropPath: String,
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
    @SerializedName("total_pages")
    val pageCount: Int,
    @SerializedName("total_results")
    val resultCount: Int
)