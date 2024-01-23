package com.kaem.flux.model.tmdb

import com.google.gson.annotations.SerializedName

enum class TMDBMediaType {
    @SerializedName("tv")
    TV,
    @SerializedName("movie")
    MOVIE
}