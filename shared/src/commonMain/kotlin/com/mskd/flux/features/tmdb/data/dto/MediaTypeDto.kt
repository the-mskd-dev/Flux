package com.mskd.flux.features.tmdb.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MediaTypeDto {
    @SerialName("tv")
    SHOW,
    @SerialName("movie")
    MOVIE,
    @SerialName("person")
    PERSON
}