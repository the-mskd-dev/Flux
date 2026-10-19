package com.mskd.flux.core.network.tmdb.data.dto.people

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the info of a person retrieved from TMDB (`GET /3/person/{id}`).
 *
 * @property adult Whether the person is an adult.
 * @property alsoKnownAs Alternative names of the person.
 * @property biography Biography of the person (may be an empty string).
 * @property birthday Birthday of the person in ISO format (`YYYY-MM-DD`), nullable.
 * @property deathday Death day of the person in ISO format (`YYYY-MM-DD`), null when alive.
 * @property gender Gender of the person (0 = unspecified, 1 = female, 2 = male, 3 = non-binary).
 * @property homepage Homepage of the person, nullable.
 * @property id Unique TMDB person identifier.
 * @property imdbId IMDB identifier of the person, nullable.
 * @property knownForDepartment Department the person is known for (e.g. `Acting`).
 * @property name Display name of the person.
 * @property placeOfBirth Place of birth of the person, nullable.
 * @property popularity Popularity score of the person.
 * @property profilePath Path to the person's profile photo, nullable.
 */
@Serializable
data class PersonInfoDto(
    val adult: Boolean = false,
    @SerialName("also_known_as")
    val alsoKnownAs: List<String> = emptyList(),
    val biography: String = "",
    val birthday: String? = null,
    val deathday: String? = null,
    val gender: Int = 0,
    val homepage: String? = null,
    val id: Int,
    @SerialName("imdb_id")
    val imdbId: String? = null,
    @SerialName("known_for_department")
    val knownForDepartment: String = "",
    val name: String = "",
    @SerialName("place_of_birth")
    val placeOfBirth: String? = null,
    val popularity: Double = 0.0,
    @SerialName("profile_path")
    val profilePath: String? = null,
)