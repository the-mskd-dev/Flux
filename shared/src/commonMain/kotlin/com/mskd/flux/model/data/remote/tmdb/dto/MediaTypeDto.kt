package com.mskd.flux.model.data.remote.tmdb.dto

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