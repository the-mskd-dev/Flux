package com.mskd.flux.core.network.tmdb.data.dto.movie

import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
 * @property releaseDate Release date of the movie as a string.
 */
@Serializable
data class MovieDto(
    val title: String,
    @SerialName("overview")
    val description: String,
    val id: Long,
    @SerialName("runtime")
    val duration: Int?,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("backdrop_path")
    val bannerPath: String?,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("release_date")
    val releaseDate: String,
    val genres: List<GenreDto>
)