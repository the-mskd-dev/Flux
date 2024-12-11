package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

/**
 * Represents a movie retrieved from TMDB.
 *
 * @property title Title of the movie.
 * @property description Overview or synopsis of the movie.
 * @property id Unique identifier for the movie.
 * @property duration Duration of the movie in minutes.
 * @property imagePath Path to the poster image of the movie.
 * @property bannerPath Path to the backdrop image of the movie.
 * @property voteAverage Average rating of the movie.
 * @property voteCount Number of votes for the movie.
 * @property releaseDateString Release date of the movie as a string.
 */
data class TMDBMovie(
    val title: String,
    @SerializedName("overview")
    val description: String,
    val id: Long,
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
    val releaseDateString: String
)
