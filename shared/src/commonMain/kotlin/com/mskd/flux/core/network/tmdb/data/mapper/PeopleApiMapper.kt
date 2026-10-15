package com.mskd.flux.core.network.tmdb.data.mapper

import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.PersonType
import com.mskd.flux.core.network.tmdb.data.dto.people.PeopleDto
import com.mskd.flux.core.network.tmdb.data.dto.people.PersonInfoDto
import com.mskd.flux.core.network.tmdb.domain.model.PersonInfo

/**
 * Maps a TMDB credits response to the unified [Person] model.
 *
 * Cast members keep their billing [CastMemberDto.order] while crew members use their
 * index, so a single table/object can hold both cast and crew with a stable ordering.
 */
fun PeopleDto.toDomain(artworkId: Long): List<Person> {

    val cast = this.cast.map { member ->
        Person(
            id = member.id,
            creditId = member.creditId,
            artworkId = artworkId,
            name = member.name,
            originalName = member.originalName,
            profilePath = member.profilePath,
            role = member.character,
            type = PersonType.CAST,
            personOrder = member.order
        )
    }

    val crew = this.crew.mapIndexed { index, member ->
        Person(
            id = member.id,
            creditId = member.creditId,
            artworkId = artworkId,
            name = member.name,
            originalName = member.originalName,
            profilePath = member.profilePath,
            role = member.job,
            type = PersonType.CREW,
            personOrder = index
        )
    }

    return cast + crew
}

/**
 * Maps a TMDB person details response to the [PersonInfo] domain model.
 *
 * Fields that TMDB declares as non-nullable but that may be blank (biography,
 * known for department) are kept as-is so the domain layer can decide whether to
 * display them or fall back to the locally available data.
 */
fun PersonInfoDto.toDomain(): PersonInfo {
    return PersonInfo(
        id = this.id,
        name = this.name,
        profilePath = this.profilePath,
        birthday = this.birthday,
        deathday = this.deathday,
        placeOfBirth = this.placeOfBirth,
        biography = this.biography,
        knownForDepartment = this.knownForDepartment,
        alsoKnownAs = this.alsoKnownAs,
        homepage = this.homepage,
        imdbId = this.imdbId,
    )
}