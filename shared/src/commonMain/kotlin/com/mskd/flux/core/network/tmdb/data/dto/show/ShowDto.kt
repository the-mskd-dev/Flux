package com.mskd.flux.core.network.tmdb.data.dto.show

import com.mskd.flux.core.network.tmdb.data.dto.genre.GenreDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowDto(
    @SerialName("name")
    val title: String,
    @SerialName("overview")
    val description: String,
    val id: Long,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("backdrop_path")
    val bannerPath: String?,
    @SerialName("vote_average")
    val voteAverage: Float,
    @SerialName("vote_count")
    val voteCount: Int,
    @SerialName("first_air_date")
    val releaseDate: String,
    val genres: List<GenreDto>
)
