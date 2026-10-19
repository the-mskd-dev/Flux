package com.mskd.flux.core.network.tmdb.domain.model

/**
 * Represents the enriched personal details of a person retrieved from the TMDB API.
 *
 * This is the API-side counterpart of the basic [com.mskd.flux.core.model.artwork.Person]
 * model: it only carries the fields that require a network call, so the domain layer can
 * merge it with the locally available data when offline.
 *
 * @property id Unique TMDB person identifier.
 * @property name Display name of the person.
 * @property profilePath Path to the person's profile photo (nullable).
 * @property birthday Birthday of the person in ISO format (`YYYY-MM-DD`) (nullable).
 * @property deathday Death day of the person in ISO format (`YYYY-MM-DD`), null when alive.
 * @property placeOfBirth Place of birth of the person (nullable).
 * @property biography Biography of the person, may be blank when not translated/available.
 * @property knownForDepartment Department the person is known for (e.g. `Acting`).
 * @property alsoKnownAs Alternative names of the person.
 * @property homepage Homepage of the person (nullable).
 * @property imdbId IMDB identifier of the person (nullable).
 */
data class PersonInfo(
    val id: Int,
    val name: String,
    val profilePath: String?,
    val birthday: String?,
    val deathday: String?,
    val placeOfBirth: String?,
    val biography: String,
    val knownForDepartment: String,
    val alsoKnownAs: List<String>,
    val homepage: String?,
    val imdbId: String?,
)