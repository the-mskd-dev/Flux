package com.mskd.flux.core.network.tmdb.data.dto.genre

import kotlinx.serialization.Serializable

@Serializable
data class GenresResultDto(
    val genres: List<GenreDto>
)
