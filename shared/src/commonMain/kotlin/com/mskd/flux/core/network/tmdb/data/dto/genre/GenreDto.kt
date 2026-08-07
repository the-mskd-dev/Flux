package com.mskd.flux.core.network.tmdb.data.dto.genre

import kotlinx.serialization.Serializable

@Serializable
data class GenreDto(
    val id: Int,
    val name: String
)