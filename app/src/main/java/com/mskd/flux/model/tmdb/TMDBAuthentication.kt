package com.mskd.flux.model.tmdb

/**
 * Represents the result of an authentication request with TMDB.
 *
 * @property success Indicates whether the authentication was successful.
 * @property status_code Status code of the authentication response.
 * @property status_message Message associated with the authentication status.
 */
data class TMDBAuthentication(
    val success: Boolean,
    val status_code: Int,
    val status_message: String
)
