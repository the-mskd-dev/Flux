package com.mskd.flux.features.people.domain.model

import com.mskd.flux.core.model.artwork.Artwork
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Represents the full details of a person, merging the basic data available locally
 * with the enriched information retrieved from the TMDB API.
 *
 * When the API is unreachable, the enriched fields keep their default values and only
 * the basic fields ([name], [profilePath]) plus [artworks] are filled,
 * which allows the screen to render an offline fallback without empty placeholders.
 *
 * @property id Unique TMDB person identifier.
 * @property name Display name of the person.
 * @property originalName Display original name of the person.
 * @property profilePath Path to the person's profile photo (nullable).
 * @property birthday Birthday of the person in ISO format (`YYYY-MM-DD`) (nullable).
 * @property deathday Death day of the person in ISO format (`YYYY-MM-DD`), null when alive.
 * @property placeOfBirth Place of birth of the person (nullable).
 * @property biography Biography of the person (nullable, blank when unavailable).
 * @property knownForDepartment Department the person is known for (e.g. `Acting`).
 * @property alsoKnownAs Alternative names of the person.
 * @property homepage Homepage of the person (nullable).
 * @property imdbId IMDB identifier of the person (nullable).
 * @property artworks Local artworks in which the person appears.
 */
data class PersonDetails(
    val id: Int,
    val name: String,
    val originalName: String?,
    val profilePath: String?,
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val biography: String? = null,
    val knownForDepartment: String? = null,
    val alsoKnownAs: List<String> = emptyList(),
    val homepage: String? = null,
    val imdbId: String? = null,
    val artworks: ImmutableList<Artwork> = persistentListOf(),
)