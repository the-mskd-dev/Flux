package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

enum class TMDBMediaType {
    @SerializedName("tv")
    SHOW,
    @SerializedName("movie")
    MOVIE
}