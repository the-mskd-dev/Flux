package com.mskd.flux.core.network.tmdb.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the result of an authentication request with TMDB.
 *
 * @property success Indicates whether the authentication was successful.
 * @property code Status code of the authentication response.
 * @property message Message associated with the authentication status.
 */
@Serializable
data class AuthenticationDto(
    val success: Boolean,
    @SerialName("status_code")
    val code: Int?,
    @SerialName("status_message")
    val message: String?
)
