package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBArtwork(
    val id: Int,
    @SerializedName("overview")
    val description: String,
    @SerializedName("poster_path")
    val imagePath: String,
    @SerializedName("backdrop_path")
    val bannerPath: String,
    @SerializedName("media_type")
    var type: TMDBMediaType,
    @SerializedName("genre_ids")
    val genres: List<Int>,
    val popularity: Float,
    @SerializedName("first_air_date")
    val releaseDateString: String,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,

    @SerializedName(value = "title", alternate = ["name"])
    val title: String,
    @SerializedName(value = "original_title", alternate = ["original_name"])
    val originalTitle: String,

    // For Persons
    @SerializedName("known_for")
    val relatedContent: List<TMDBArtwork>
)

data class TMDBArtworksResult(
    val page: Int,
    val results: List<TMDBArtwork>,
    @SerializedName("total_pages")
    val pageCount: Int,
    @SerializedName("total_results")
    val resultCount: Int
)