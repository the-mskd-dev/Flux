package com.mskd.flux.core.network.tmdb.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeasonDto(
    val id: Long,
    @SerialName("name")
    val title: String,
    @SerialName("overview")
    val description: String,
    @SerialName("poster_path")
    val imagePath: String?,
    @SerialName("season_number")
    val season: Int,
    val episodes: List<EpisodeDto>
)
