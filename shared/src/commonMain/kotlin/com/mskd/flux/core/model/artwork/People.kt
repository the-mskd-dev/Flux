package com.mskd.flux.core.model.artwork

/**
 * Represents a person associated with an artwork, either as a cast member or a crew member.
 *
 * A single unified model is used for both the cast and the crew so that the app only
 * depends on one object and one database table.
 *
 * @property id Unique TMDB person identifier.
 * @property creditId Unique credit identifier of this person for the artwork (used as primary key).
 * @property artworkId Identifier of the parent artwork (movie or show).
 * @property name Display name of the person.
 * @property profilePath Path to the person's profile photo (nullable).
 * @property role Character (cast) or job (crew) of the person.
 * @property type Whether the person belongs to the cast or the crew.
 * @property personOrder Billing / display order of the person.
 */
data class People(
    val id: Int,
    val creditId: String,
    val artworkId: Long,
    val name: String,
    val profilePath: String?,
    val role: String,
    val type: PeopleType,
    val personOrder: Int = 0,
)

enum class PeopleType {
    CAST,
    CREW,
}
