package com.kaem.flux.model.tmdb

data class TMDBMedia(
    val backdrop_path: String,
    val id: Int,
    val name: String,
    val overview: String,
    val poster_path: String,
    val media_type: TMDBMediaType,
    val genre_ids: List<Int>,
    val popularity: Float,
    val first_air_date: String,
    val vote_average: Float,
    val vote_count: Int
)

data class TMDBMediasResult(
    val page: Int,
    val results: List<TMDBMedia>,
    val total_pages: Int,
    val total_results: Int
)