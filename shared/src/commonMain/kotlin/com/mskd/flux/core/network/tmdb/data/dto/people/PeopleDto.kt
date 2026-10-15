package com.mskd.flux.core.network.tmdb.data.dto.people

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the people (cast + crew) of a movie or TV show retrieved from TMDB.
 *
 * @property id Unique identifier of the artwork the people belong to.
 * @property cast List of cast members.
 * @property crew List of crew members.
 */
@Serializable
data class PeopleDto(
    val id: Long,
    val cast: List<CastMemberDto>,
    val crew: List<CrewMemberDto>,
)

/**
 * Represents a cast member of a movie or TV show.
 *
 * @property id Unique TMDB person identifier.
 * @property name Display name of the person.
 * @property profilePath Path to the person's profile photo (nullable).
 * @property creditId Unique credit identifier.
 * @property character Character played by the person.
 * @property order Billing order of the person within the cast.
 */
@Serializable
data class CastMemberDto(
    val id: Int,
    val name: String,
    @SerialName("profile_path")
    val profilePath: String? = null,
    @SerialName("credit_id")
    val creditId: String,
    val character: String = "",
    val order: Int = 0,
)

/**
 * Represents a crew member of a movie or TV show.
 *
 * @property id Unique TMDB person identifier.
 * @property name Display name of the person.
 * @property profilePath Path to the person's profile photo (nullable).
 * @property creditId Unique credit identifier.
 * @property job Job of the person.
 * @property department Department of the person.
 */
@Serializable
data class CrewMemberDto(
    val id: Int,
    val name: String,
    @SerialName("profile_path")
    val profilePath: String? = null,
    @SerialName("credit_id")
    val creditId: String,
    val job: String = "",
    val department: String = "",
)