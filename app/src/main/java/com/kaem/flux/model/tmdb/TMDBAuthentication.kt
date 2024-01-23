package com.kaem.flux.model.tmdb

data class TMDBAuthentication(
    val success: Boolean,
    val status_code: Int,
    val status_message: String
)
