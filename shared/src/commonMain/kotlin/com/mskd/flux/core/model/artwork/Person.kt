package com.mskd.flux.core.model.artwork

import kotlinx.serialization.Serializable

/**
 * Represents a person associated with an artwork, either as a cast member or a crew member.
 *
 * A single unified model is used for both the cast and the crew so that the app only
 * depends on one object and one database table.
 *
 * The model is [Serializable] so it can be carried inside a navigation route
 * (`Route.Person`), which enables an instant offline fallback on the details screen.
 *
 * @property id Unique TMDB person identifier.
 * @property creditId Unique credit identifier of this person for the artwork (used as primary key).
 * @property artworkId Identifier of the parent artwork (movie or show).
 * @property name Display name of the person.
 * @property originalName Display original name of the person.
 * @property profilePath Path to the person's profile photo (nullable).
 * @property role Character (cast) or job (crew) of the person.
 * @property type Whether the person belongs to the cast or the crew.
 * @property personOrder Billing / display order of the person.
 */
@Serializable
data class Person(
    val id: Int,
    val creditId: String,
    val artworkId: Long,
    val name: String,
    val originalName: String?,
    val profilePath: String?,
    val role: String,
    val type: PersonType,
    val personOrder: Int = 0,
)

@Serializable
enum class PersonType {
    CAST,
    CREW,
}
